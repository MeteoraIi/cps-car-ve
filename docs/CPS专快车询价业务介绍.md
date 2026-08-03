# CPS 专快车出车服务 — 询价业务介绍

## 一、系统定位

这套代码属于 **CPS（Car Purchase System，车辆采购系统）**，是一个 **企业出行用车聚合平台的采购核心模块**。CPS 上游对接采购商系统（ASMS），下游通过 LINK 系统聚合了多家出行供应商（滴滴、高德、首汽、曹操、T3、哈啰等），为企业客户提供一站式的专车/快车比价和预订服务。

## 二、整体架构（四层模块）

| 模块 | 职责 |
|---|---|
| **cps-usecar-openapi** | 对外开放的 API 接口层，供 ASMS 采购系统调用 |
| **cps-usecar-service** | 核心业务逻辑层，询价、控润、缓存、优惠券匹配等 |
| **cps-usecar-common** | 公共常量、枚举定义 |
| **cps-car-common-service** | 供应商基础数据（接口配置、数据字典映射） |

**调用链路**：

```
ASMS → CPS-OpenAPI → CPS-Service → LINK → 各出行供应商（滴滴/高德/首汽/曹操/T3/哈啰等）
```

## 三、询价业务核心流程

询价是整个出车服务的入口，由一个 OpenAPI 接口承载：

- **接口名**：`car_searchSpecialCarProductList`（专车产品查询列表）
- **入口类**：`BuyerSpecialCarService.java`

### 第1步：接收询价请求

ASMS 传入请求参数（`BuyerSpecialCarRequest`），关键字段包括：

| 类别 | 字段 | 说明 |
|---|---|---|
| 上下车地点 | `sccs` / `mdcs` | 上下车城市ID |
| | `sccsPoi` / `mdcsPoi` | 上下车POI |
| | `scjd/scwd` / `sdjd/sdwd` | 上下车经纬度（高德坐标） |
| | `soso_cfd_x/y` / `soso_mdd_x/y` | 上下车经纬度（腾讯SOSO坐标） |
| 用车时间 | `ycrq` / `ycsj` | 用车日期+时间，空则为立即用车 |
| 车型 | `cx` | 舒适型/豪华型/商务型 |
| 计价模式 | `jjmslb` | 201:专车 301:快车 |
| 渠道 | `qdly` | ASMS / CPS |
| 扩展参数 | `paxNum` | 乘坐人数 |
| | `rideShare` | 是否查询顺风车 |
| | `sfcxyhq` | 是否查询优惠券 |
| | `queryMemberPrice` | 是否查询会员价 |
| | `cursorId` / `asyn` | 异步游标分页 |

### 第2步：供应商过滤

在 `BuyerFilterSetService` 中，系统根据**采购商过滤规则**筛选可用的供应商列表。规则支持三种管理方式：

- **全部供应商（cgsglfs=1）**：不做限制，所有供应商都可见
- **指定供应商（cgsglfs=2）**：精确指定哪些供应商可用
- **供应商组（cgsglfs=3）**：按分组批量管理供应商

同时支持**指定站点（城市/机场/火车站）**维度的过滤，并区分两种规则逻辑：

- **gllx=1**：过滤型——命中规则的供应商被排除
- **gllx=2,3**：保留型——只有命中规则的供应商才保留

**规则优先级匹配**：当同一采购商有多条规则时，系统自动选取最优规则（保留型 > 过滤型；指定采购商 > 指定分组 > 全部采购商；审批时间越新越优先）。

### 第3步：调用 LINK 聚合询价

在 `BuyerBookSpeciCarProductService.getZcLinkCpsList()` 中：

1. 将请求参数转换为 LINK 系统格式
2. 进行**城市编码映射**——CPS 内部城市ID 转换为各供应商的城市ID（滴滴、T3、高德不需要城市映射，首汽、曹操等需要查数据字典转换）
3. 通过 **Feign** 远程调用 `ILinkSpecialCarServiceClient` 向 LINK 发起询价
4. LINK 并发查询各出行供应商，返回每个供应商的预估价格、距离、时长
5. **同供应商同车型低价过滤**：如果开启了低价过滤（`sfkqdjgl=1`），同供应商同车型只保留最低价

### 第4步：控润 & 贴点 & 返佣计算（核心定价逻辑）

这是 CPS 平台的核心价值——**在供应商原始报价基础上，叠加平台的利润策略**。在 `BuyerBookSpeciCarCommonService.productKrSetting()` 中完成。

**三个定价因子：**

| 因子 | 字段 | 含义 | 计算方式 |
|---|---|---|---|
| **控润（kr）** | `ptkrfs/ptkrbl/ptkrje` | 平台在供应价基础上加的利润 | 固定金额 / 百分比 / 百分比+固定值 |
| **贴点（td）** | `pttdfs/pttdbl/pttdje` | 平台给采购商的补贴让利 | 按金额 / 按比例 |
| **返佣（fy）** | `gyFyfs/gyFybl/gyFyje` | 供应商给平台的佣金 | 前返（下单时扣除）/ 后返（完成后结算） |

**最终采购结算价公式**：

```
采购结算价 = 建议销售价 - 贴点金额 + 控润金额
```

**一口价产品**：`采购结算价 = 结算价 + 控润 - 贴点`

额外支持：

- **会员折扣**：根据会员等级（铂金/金/银等）享受不同折扣率
- **城市等级**：不同城市可以配置不同的控润策略
- **渠道差异**：不同渠道（channelId）可有独立定价

### 第5步：价格明细均摊

在 `savePriceDetail()` 方法中，将控润和"企业服务费"、"信息费"等费用按比例均摊到**里程费**和**时长费**上，保证用户端看到的价格明细合理且透明。

- 里程费和时长费都存在 → 各摊 50%
- 只有里程费 → 全部摊入里程费
- 只有时长费 → 全部摊入时长费
- 都不存在 → 新建"调度费"项

### 第6步：价格缓存

在 `BuyerBookSpeciCarCacheService` 中，将询价结果以 `cursorId-priceCacheId` 为 Key 写入 Redis 缓存，有效期默认 **10 分钟**。下单时直接从缓存取价格，避免价格波动导致结算不一致。

### 第7步：优惠券匹配（可选）

如果请求携带 `sfcxyhq=true`，系统会调用 `CouponConsumeService`：

- 查询用户**可用优惠券**（满减券/折扣券）
- 查询用户**未领取的礼赠券**
- 按订单类型、车型、用车城市、有效期限、不可用日期、最低消费门槛逐一过滤
- 自动选取**优惠金额最大**的一张券返回
- 返回券后预估价格：`预估金额 - 优惠金额`

### 第8步：返回询价结果

最终返回 `BuyerSpecialCarResponse`，包含：

| 返回内容 | 说明 |
|---|---|
| **产品列表 cplist** | 每个产品包含：供应商名称/logo、车型组、一口价/预估价、结算价、控润/贴点/返佣明细、取消规则、价格明细JSON、可用优惠券、礼赠券等 |
| **路程信息** | 预估距离（米）、预估时长（分钟） |
| **供应商推荐** | 根据供应商分析数据推荐优先供应商 |
| **异步加载** | `cursorId` + `finishFlag` 支持分批加载大量结果 |
| **缓存超时** | `cacheTimeout` 告知调用方缓存有效期 |

## 四、下单业务（简述）

代码中还有 `CreateOrderService`，是询价之后的**下单接口**（`car_createOrder`），核心流程：

1. **黑名单检查**：检查乘车人是否在用车黑名单中
2. **风控检查**：B2C/CPSC-PC 渠道检查用户取消率
3. **未付订单限制**：检查是否有未支付的违约订单
4. **订单分流**：
   - 专快车（ddlx=10000501）→ `createOrderV1Service.specialCarSubmitOrderToCps()`
   - 接送车 → `createOrderV1Service.shuttleCarSubmitOrderToCps()`
   - V2 版本 → `createOrderV2Service.createOrder()`
   - 包车 → 简化下单流程
5. **冻结优惠券**：下单成功后冻结用户使用的优惠券

## 五、核心类说明

| 类 | 路径 | 职责 |
|---|---|---|
| `BuyerSpecialCarService` | openapi/.../specialcar/ | 询价接口入口，参数转换，结果组装 |
| `BuyerSpecialCarRequest` | openapi/.../specialcar/ | 询价请求参数模型 |
| `BuyerSpecialCarResponse` | openapi/.../specialcar/ | 询价响应模型 |
| `BuyerSpecialCar` | openapi/.../specialcar/ | 单个产品模型（供应商+价格+控润+券） |
| `BuyerBookSpeciCarProductService` | service/.../specicar/ | 核心询价逻辑，调用LINK，控润计算 |
| `BuyerBookSpeciCarCommonService` | service/.../specicar/ | 控润/贴点/返佣计算，取消规则匹配 |
| `BuyerBookSpeciCarCacheService` | service/.../specicar/ | 询价结果Redis缓存 |
| `BuyerFilterSetService` | setting/buyerfilter/ | 采购商供应商过滤规则 |
| `ProfitSettingService` | service/.../specicar/ | 控润开关配置 |
| `SpecialCarPriceBean` | openapi/.../bean/ | 价格明细项（费用项目+金额） |
| `CouponConsumeService` | coupon/ | 优惠券查询、匹配、冻结、核销、退回 |
| `CreateOrderService` | openapi/.../createorder/ | 下单接口入口 |
| `ILinkSpecialCarServiceClient` | apiclient/linkusecar/ | LINK系统Feign调用接口 |
| `UseCarConstant` | common/ | 全系统常量定义 |

## 六、一句话总结

> 这是一个**企业出行聚合平台的采购询价系统**，上游对接企业采购系统（ASMS），下游聚合滴滴、高德、首汽等多家出行供应商，核心能力是在供应商原始报价基础上进行**控润、贴点、返佣三重定价计算**，并叠加**供应商过滤、优惠券匹配、会员折扣**等策略，最终为企业客户提供最优的专车/快车出行报价。
