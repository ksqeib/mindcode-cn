# Mindcode

欢迎来到Mindcode，一款为[Mindustry](https://github.com/anuke/mindustry)设计的高级语言。Mindustry是一款塔防类游戏。
Mindustry在2020年末增加了Logic，Logic是一个接近更汇编而不是高级语言的语言。 Mindcode旨在让每个人都能更轻松地进行Mindustry编程。

Mindcode的设计考虑了两大重要方面:
* 保持术语和命名约定接近Mindustry Logic。
* 考虑到Mindustry中有限的命名空间和惊人的缓慢速度，提供开销不是很大的高级语言结构。

## 最新进展

一些Mindcode的新版本增强有:

* 增加了对新功能[`angleDiff` 操作符](doc/syntax/FUNCTIONS_V7.markdown#instruction-op) Mindustry v145 的支持。
* [蓝图构建器](doc/syntax/SCHEMACODE.markdown)。 该工具允许您使用一种特殊的定义语言Schemacode定义一个Mindustry蓝图。Schemacode的定义可被编译为Mindustry蓝图，
  或者是二进制文件`.msch`，亦或是普通的文本。处理器可以被包括在蓝图中并且带有编译的代码
  (可以指定Mindcode或者Mindustry Logic)以及方块之间的连接。
* [数据流优化](doc/syntax/SYNTAX-5-OTHER.markdown#data-flow-optimization)。这种优化优化和精简了复杂的表达式，消除了不必要的变量和指令，并重用了常见的cde片段，显著提高了生成的mlog代码的质量。

一个 [更新日志](CHANGELOG.markdown) 现在被维护以供发布。

## 使用Mindcode

Mindcode网页被架设在 http://mindcode.herokuapp.com/ 。写一些Mindcode在 _Mindcode源码_ 框的地方，
然后按下 **编译(Compile)** 按钮。 然后在 _Mindustry Logic_ 文本框中你需要的 Logic 版本的 Mindcode就有了。
复制编译后的版本。 回到Mindustry，编辑你的处理器，然后按下Logic UI中的**编辑**按钮。
选择**从剪贴板导入**。Mindustry现在就已经准备好执行你的代码了。

也可以在本地编译Mindcode(查看[开发](#开发)章节)，并且使用
[命令行工具](doc/syntax/TOOLS-CMDLINE.markdown)来编译你的文件，
甚至可以根据需要自动将编译后的代码复制到剪贴板中。

### Mindcode语法

请阅读 [语法](doc/syntax/SYNTAX.markdown) 文档来获取高级信息。
`src/main/resources/samples`目录中的示例文件都会随着最近新版本的Mindcode更新在测试运行时被编译
如果你用过任何高级语言编程，你应该
感觉宾至如归。

### VS Code语法高亮

[@schittli](https://github.com/schittli) 好心地贡献了一个VS Code语法高亮。

![Visual Studio代码的屏幕截图，Mindcode语法高亮](https://user-images.githubusercontent.com/8282673/112750180-43947a00-8fc7-11eb-8a22-83be7624753e.png)

从[Visual Studio 市场](https://marketplace.visualstudio.com/items?itemName=TomSchi.mindcode)中下载扩展。
我不确定这个扩展对Mindcode最新添加内容的支持程度好不好。

### IntelliJ IDEA语法高亮

IntelliJ IDEA (甚至是社区版) 可以很容易地配置出基本的Mindcode语法高亮显示。

* 找到 _File / Settings_，或者按下 Ctrl-Alt-S
* 引导到 _Editor / File types_
* 创建新的文件类型
  * _Name_: Mindcode
  * _Description_: Mindcode源文件
  * _Line comment_: `//` (让 _Only at line start_ 不要勾选)
  * _Block comment start/end_: 留空
  * _Hex prefix_: `0x`
  * _Number postfixes_: 留空
  * _Keywords_: 将Mindcode关键字粘贴到第一个列表中。（可选）将Mindusty Logic对象名称粘贴到第二个列表中。
  * _Ignore case_: 保留不勾选
* Assign a file extension `*.mnd`

<details><summary>显示Mindcode关键字的完整列表。</summary>

```
allocate
and
break
case
const
continue
def
do
else
elsif
end
false
for
heap
if
in
inline
loop
not
null
or
return
sensor
stack
then
true
when
while
```

</details>

<details><summary>显示Mindusty Logic对象名称的完整列表。</summary>

```
@additive-reconstructor
@aegires
@afflict
@air
@air-factory
@alpha
@ammo
@ammoCapacity
@anthicus
@antumbra
@arc
@arkycite
@arkycite-floor
@arkyic-boulder
@arkyic-stone
@arkyic-vent
@arkyic-wall
@arkyid
@armored-conveyor
@armored-duct
@atmospheric-concentrator
@atrax
@avert
@barrier-projector
@basalt
@basalt-boulder
@basic-assembler-module
@battery
@battery-large
@beam-link
@beam-node
@beam-tower
@beryllic-boulder
@beryllic-stone
@beryllic-stone-wall
@beryllium
@beryllium-wall
@beryllium-wall-large
@beta
@blast-compound
@blast-door
@blast-drill
@blast-mixer
@bluemat
@boosting
@boulder
@breach
@bridge-conduit
@bridge-conveyor
@bryde
@build-tower
@canvas
@carbide
@carbide-crucible
@carbide-wall
@carbide-wall-large
@carbon-boulder
@carbon-stone
@carbon-vent
@carbon-wall
@char
@chemical-combustion-chamber
@cleroi
@cliff
@cliff-crusher
@coal
@coal-centrifuge
@collaris
@color
@combustion-generator
@command-center
@commanded
@conduit
@config
@configure
@conquer
@constructor
@container
@controlled
@controller
@conveyor
@copper
@copper-wall
@copper-wall-large
@core-acropolis
@core-bastion
@core-citadel
@core-foundation
@core-nucleus
@core-shard
@core-zone
@corvus
@counter
@crater-stone
@crawler
@cryofluid
@cryofluid-mixer
@crystal-blocks
@crystal-cluster
@crystal-floor
@crystal-orbs
@crystalline-boulder
@crystalline-stone
@crystalline-stone-wall
@crystalline-vent
@cultivator
@cyanogen
@cyanogen-synthesizer
@cyclone
@cyerce
@dacite
@dacite-boulder
@dacite-wall
@dagger
@dark-metal
@darksand
@darksand-tainted-water
@darksand-water
@dead
@deconstructor
@deep-tainted-water
@deep-water
@dense-red-stone
@differential-generator
@diffuse
@diode
@dirt
@dirt-wall
@disassembler
@disperse
@disrupt
@distributor
@door
@door-large
@dormant
@duct
@duct-bridge
@duct-router
@duct-unloader
@dune-wall
@duo
@eclipse
@efficiency
@electric-heater
@electrolyzer
@elude
@emanate
@empty
@enabled
@eruption-drill
@evoke
@exponential-reconstructor
@ferric-boulder
@ferric-craters
@ferric-stone
@ferric-stone-wall
@firstItem
@fissile
@flag
@flare
@flux-reactor
@force-projector
@foreshadow
@fortress
@fuse
@gamma
@graphite
@graphite-press
@graphitic-wall
@grass
@ground-factory
@hail
@health
@heat
@heat-reactor
@heat-redirector
@heat-router
@heat-source
@horizon
@hotrock
@hydrogen
@hyper-processor
@ice
@ice-snow
@ice-wall
@illuminator
@impact-drill
@impact-reactor
@impulse-pump
@incinerator
@incite
@interplanetary-accelerator
@inverted-sorter
@item-source
@item-void
@itemCapacity
@junction
@kiln
@lancer
@large-constructor
@large-logic-display
@large-payload-mass-driver
@large-plasma-bore
@large-shield-projector
@laser-drill
@launch-pad
@lead
@legacy-mech-pad
@legacy-unit-factory
@legacy-unit-factory-air
@legacy-unit-factory-ground
@liquid-container
@liquid-junction
@liquid-router
@liquid-source
@liquid-tank
@liquid-void
@liquidCapacity
@locus
@logic-display
@logic-processor
@lustre
@mace
@magmarock
@malign
@mass-driver
@maxHealth
@mech-assembler
@mech-fabricator
@mech-refabricator
@mechanical-drill
@mechanical-pump
@mega
@meltdown
@melter
@memory-bank
@memory-cell
@mend-projector
@mender
@merui
@message
@metaglass
@metal-floor
@metal-floor-damaged
@micro-processor
@mineX
@mineY
@mining
@minke
@minute
@molten-slag
@mono
@moss
@mud
@multi-press
@multiplicative-reconstructor
@name
@naval-factory
@navanax
@neoplasia-reactor
@neoplasm
@nitrogen
@nova
@obviate
@oct
@oil
@oil-extractor
@omura
@ore-crystal-thorium
@ore-wall-beryllium
@ore-wall-thorium
@ore-wall-tungsten
@overdrive-dome
@overdrive-projector
@overflow-duct
@overflow-gate
@oxidation-chamber
@oxide
@oxynoe
@ozone
@parallax
@payload-conveyor
@payload-loader
@payload-mass-driver
@payload-router
@payload-source
@payload-unloader
@payload-void
@payloadCount
@payloadType
@pebbles
@phase-conduit
@phase-conveyor
@phase-fabric
@phase-heater
@phase-synthesizer
@phase-wall
@phase-wall-large
@phase-weaver
@pine
@plasma-bore
@plastanium
@plastanium-compressor
@plastanium-conveyor
@plastanium-wall
@plastanium-wall-large
@plated-conduit
@pneumatic-drill
@poly
@pooled-cryofluid
@power-node
@power-node-large
@power-source
@power-void
@powerCapacity
@powerNetCapacity
@powerNetIn
@powerNetOut
@powerNetStored
@precept
@prime-refabricator
@progress
@pulsar
@pulse-conduit
@pulverizer
@pur-bush
@pyratite
@pyratite-mixer
@pyrolysis-generator
@quad
@quasar
@quell
@radar
@range
@red-diamond-wall
@red-ice
@red-ice-boulder
@red-ice-wall
@red-stone
@red-stone-boulder
@red-stone-vent
@red-stone-wall
@redmat
@redweed
@regen-projector
@regolith
@regolith-wall
@reign
@reinforced-bridge-conduit
@reinforced-conduit
@reinforced-container
@reinforced-liquid-container
@reinforced-liquid-junction
@reinforced-liquid-router
@reinforced-liquid-tank
@reinforced-message
@reinforced-payload-conveyor
@reinforced-payload-router
@reinforced-pump
@reinforced-surge-wall
@reinforced-surge-wall-large
@reinforced-vault
@repair-point
@repair-turret
@retusa
@rhyolite
@rhyolite-boulder
@rhyolite-crater
@rhyolite-vent
@rhyolite-wall
@ripple
@risso
@rotary-pump
@rotation
@rough-rhyolite
@router
@rtg-generator
@salt
@salt-wall
@salvo
@sand
@sand-boulder
@sand-floor
@sand-wall
@sand-water
@scathe
@scatter
@scepter
@scorch
@scrap
@scrap-wall
@scrap-wall-gigantic
@scrap-wall-huge
@scrap-wall-large
@second
@segment
@sei
@separator
@shale
@shale-boulder
@shale-wall
@shallow-water
@shield-projector
@shielded-wall
@ship-assembler
@ship-fabricator
@ship-refabricator
@shock-mine
@shockwave-tower
@shootX
@shootY
@shooting
@shrubs
@silicon
@silicon-arc-furnace
@silicon-crucible
@silicon-smelter
@size
@slag
@slag-centrifuge
@slag-heater
@slag-incinerator
@small-deconstructor
@smite
@snow
@snow-boulder
@snow-pine
@snow-wall
@solar-panel
@solar-panel-large
@sorter
@space
@spawn
@spectre
@speed
@spiroct
@spore
@spore-cluster
@spore-moss
@spore-pine
@spore-pod
@spore-press
@spore-wall
@steam-generator
@stell
@stone
@stone-wall
@sublimate
@surge
@surge-alloy
@surge-conveyor
@surge-crucible
@surge-router
@surge-smelter
@surge-tower
@surge-wall
@surge-wall-large
@swarmer
@switch
@tainted-water
@tank-assembler
@tank-fabricator
@tank-refabricator
@tar
@team
@tecta
@tendrils
@tetrative-reconstructor
@thermal-generator
@thorium
@thorium-reactor
@thorium-wall
@thorium-wall-large
@thruster
@tick
@time
@timescale
@titan
@titanium
@titanium-conveyor
@titanium-wall
@titanium-wall-large
@totalItems
@totalLiquids
@totalPower
@toxopid
@tsunami
@tungsten
@tungsten-wall
@tungsten-wall-large
@turbine-condenser
@type
@underflow-duct
@underflow-gate
@unit
@unit-cargo-loader
@unit-cargo-unload-point
@unit-repair-tower
@unloader
@vanquish
@vault
@vela
@vent-condenser
@vibrant-crystal-cluster
@water
@water-extractor
@wave
@white-tree
@white-tree-dead
@world-cell
@world-message
@world-processor
@x
@y
@yellow-stone
@yellow-stone-boulder
@yellow-stone-plates
@yellow-stone-vent
@yellow-stone-wall
@yellowcoral
@zenith
```

</details>

## Mindustry Logic参考

如果你对Mindusty Logic了解不多，你可以在这里阅读更多关于它们的信息：

* [6.0中的Logic](https://www.reddit.com/r/Mindustry/comments/ic9wrm/logic_in_60/) <small>2020年8月</small>
* [如何在6.0中使用处理器](https://steamcommunity.com/sharedfiles/filedetails/?id=2268059244) <small>2020年11月</small>
* [超深度逻辑指南](https://www.reddit.com/r/Mindustry/comments/kfea1e/an_overly_indepth_logic_guide/) <small>2020年12月</small>

还有一个 [用于Mindusty Logic的VSCode语法高亮器](https://marketplace.visualstudio.com/items?itemName=Antyos.vscode-mlog).

## 开发

有两种选择可以让Mindcode在您自己的机器上运行。使用Docker或本机运行：

### 使用Docker和Docker Compose

```
docker-compose up --build
```

第一次运行时可能需要几分钟的时间来下载和编译所有需要的部分，但随后会快很多。
Mindcode UI现在将在本地主机8080端口上运行。用浏览器打开 http://localhost:8080/ 即可使用。

### 本机安装

1. 安装Java 17+、Maven 3.6和PostgreSQL
2. 在PostgreSQL中创建一个名为`mindcode_development`的数据库

注意：Docker配置已经更新，允许在本地PostgreSQL安装的同时在Docker中运行Mindcode。

#### Windows 

使用PostgreSQL连接参数设置环境变量。您可以通过运行以下程序来设置它们 

控制台中的命令：

```
SET SPRING_DATASOURCE_URL=jdbc:postgresql://localhost/mindcode_development
SET SPRING_DATASOURCE_USERNAME=postgres_username
SET SPRING_DATASOURCE_PASSWORD=postgres_password
```

您还需要设置一个`JAVA_HOME`变量，指向包含JAVA 17安装的目录，
举个例子（具体路径取决于您安装的Java的发行版和版本）：

```
SET JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.6.10-hotspot
```

（您也可以在_系统属性_对话框中的_高级_选项卡中，然后按下_环境变量…_按钮。）

然后，使用相同的控制台窗口，运行：

```
bin\webapp.bat
```

Mindcode UI现在将在本地主机8080端口上运行。用浏览器打开 http://localhost:8080/ 即可使用。

#### Linux

给PostgreSQL设置连接参数相关的环境变量：

```
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost/mindcode_development
export SPRING_DATASOURCE_USERNAME=postgres_username
export SPRING_DATASOURCE_PASSWORD=postgres_password
```

然后运行：

```
bin/run-local
```

Mindcode UI现在将在本地主机8080端口上运行。用浏览器打开 http://localhost:8080/ 即可使用。

#### IDE

要为您的IDE运行应用程序，请如上所述设置环境变量（某些IDE允许设置它们
仅在IDE中），并将启动类设置为`info.teksol.mindcode.webapp.WebappApplication`。当您运行或调试项目中，
Mindcode UI现在将在本地主机8080端口上运行。用浏览器打开 http://localhost:8080/ 即可使用。

### 如何贡献

编译器是以测试驱动开发的方式编写的。如果可以，请查看`src/main/test`并尝试
模仿现有的测试，这样我们就可以证明是按你的预期运行的。

`info.teksol.mindcode.processor`包中的测试特别有用。他们编译并运行脚本
在模拟处理器上，将`print`指令产生的值与预期值进行比较。处理器模拟器
无法执行与Mindusty世界接口的指令-除了Memory Bank和MemoryCell-但它可以
处理各种Mindcode控制元素。使用循环实现一些更复杂的算法，
条件语句和/或函数帮助很大。（模拟处理器的运行速度比Mindcode快得多
处理器，因此运行更复杂的算法是可行的。）

## 路线图

或者是一份愿望清单，可以在[这里](ROADMAP.markdown)被找到哦。

# 许可证

MIT。有关许可证的全文，请参阅LICENSE文件。
