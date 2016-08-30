#ExtrameFunctionCalculator
=====
##简介

	ExtrameFunctionCalculator是一个功能看起来强大(？迷)的计算器,使用者能够声明变量甚至于自定义一个函数来计算。
目前能够正常使用，但因为正在实现添加技能或者未进行大规模测试。使用者请勿将此正式投入其他重要的项目当中，后果自负。
当然如果使用者觉得这货太sh[和谐]t了，那么可以使用隔壁的[BaseCalculator](https://github.com/MikiraSora/BaseCalculator)来进行四则运算，ExtrameFunctionCalculator则是
由此基础撸成的.

##语法列举/使用方式    
------
(note :以下代码均可在ExtrameFunctionCalculator.Execute()使用)

reg 函数名(函数参数1,函数参数2,函数参数3...函数参数n)=表达式<br>
reg用来声明一个函数,例如:<br>
```java
	reg f(x)=x*10<br>
	reg f(x,y,z)=g(x)+t(x,y)+o(f(z))<br>
  
```

set 变量名=表达式<br>
set用来定义声明变量,例如:<br>
```java
set x=6
  set y=x
  set x=f(6) //此时函数f开始计算值才赋给变量
```


solve 表达式<br>
solve用来计算表达式 例如:<br>
```java
  solve 6  //输出6
  solve x //输出变量x的值(如果变量代表的是表达式则是计算此表达式并得到值)
  solve f(100)+g(h(x))
```

dump 参数(指定类型)<br>
dump用来打印指定的信息内容，注意参数只能输入一个,可用参数如下:<br>
  "-rf"或者"raw_function" :将打印出所有内建函数(不包括函数定义内容)<br>
  "-cf"或者"custom_function" :将打印出所有自己定义的函数(包括函数定义内容)<br>
  "-var"或者"variable" :将打印出所有自己定义的变量<br>
  "-all"或者"all" :将打印出以上所有内容<br>
例如:
```java
  dump -cf
  dump variable
```

set_expr 表达式名=表达式<br>
set_expr类似于set，声明一个变量但是它储存表达式，前者set_expr可以不立即计算，后者set就必须立即计算，例如:<br>
```java
  set_expr myexpr=a+b-f(x)//此时变量a和b,以及函数f()均为声明，但因为是声明一个表达式变量myexpr，并未开始计算，所以是可以的
  set_expr refmyexpr=myexpr//ref_myexpr将会引用于myexpr的表达式
```

delete 参数(指定类型) 名字<br>
delete可以删除某个变量或者函数(不能删除内建函数),可用参数如下:<br>
  "function" :在查找并删除指定的函数(不包括内建函数)<br>
  "variable" :在查找并删除指定的变量<br>
例如:<br>
```java
  delete variable myvar
```

*clear<br>
clear <br>
clear会刷新计算器(并不会清除已经定义的函数或者变量)<br>

*reset<br>
reset <br>
reset会重置计算器，清空并删除已经定义的函数或者变量(除了内建函数),顺带执行clear命令<br>

*save<br>
save 参数(指定类型) 保存路径<br>
save用来保存已声明的函数(暂不支持反射函数)或者变量,注意参数只能输入一个,注意IO权限,可用参数如下:<br>
  "function" :将保存出所有内建函数(不包括反射函数)<br>
  "variable" :将保存出所有变量<br>
  "all" :将保存以上内容(还是不包括反射函数:D)<br>
例如:
```java
  save function C:\e.opt
  save function \a.8s8s8s
```

*load<br>
load 读取路径<br>
load用来读取前者save保存的文件，因为save保存时已经分开类型所以load会自行判断并读取，加载,注意IO权限。例如:<br>
```java
  load F:\mysave.opt
```
