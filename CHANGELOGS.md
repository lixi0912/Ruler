## 1.0-alpha3 (2019/03/22)

### Bug Fixes
- 修复宽高不变的情况下，由于重新触发了测量，产生不必要的界面移动动画
- 修复首次绘制时，使用了 scroller 导致不必要的移动动画
- 修复 xml 配置参数时，options 里的默认参数覆盖了 RulerView 的配置 

### Changes
- 宽高不变的情况下，requestLayout 内部计算类将不会重新测量，用于避免不必要的计算


## 1.0-alpha2 (2019/03/21)
### Enhancements
- RulerView 支持固定宽高
- 增加文字可自适应大小，支持模式
  - never 关闭自适应大小
  - always 每个 label 都测量
  - longest 只测量最长的 label

### Bug Fixes
- 修复 1.0-alpha01 中不支持固定宽高
### Changes
- 不在强制在 grade.properties 中配置发布仓库的信息配置
- 如使用 include deploy 模式，可能会出现 sync failed，建议改用新版方式，命令行多指定目录（AS 3.4 Cancry 版本修复）


## 1.0-alpha01 (2019/03/11)
### Enhancements
- RulerView 基本显示
- 支持设置刻度尺显示方为（enableMirrorTick = true 时，刻度显示为 start & end）
  - horizontal - start|end，刻度显示在上/下
  - vertical  - start|end，刻度显示在左/右
- 支持格式化 Label，如格式化成: 10.0  哈哈  20  呵呵
- 支持配置两个大刻度中间的小刻度，相较于小刻度与大刻度的权重值
``` 
┆       ┆
┆   |   ┆
┆ | | | ┆
    o   
```
- 支持配置文字显示方向
```
     |LEFT  
RIGHT|       
  CEN|TER                  
```
- 支持配置文字，刻度线，刻度间隔线的权重及样式
- 支持配置当前选中项样式的配置

### Bugs
- 固定宽高导致异常
