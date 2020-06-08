### 1. 名词解释
* *`NFU：`* Near Field Upgrade 的缩写

### 2. NFU 接口介绍
* *`checkVersion()：`* 检测车机版本信息

* *`preDownload()：`* 预下载车机升级差分包

* *`startUpgrade(ip: String)：`* 通过给定 IP 地址与车机连接进行升级工作

### 3. 使用示例
* *`checkVersion()`*
  * 自动管理生命周期
    ```kotlin
    NFU.checkVersion()
            // 当侦听到 onDestroy 事件自动取消任务和订阅
            .autoDispose(lifecycle)
            // 订阅
            .subscribe { versionMsg ->
                // 根据 versionMsg 进行一些业务工作
                
                // 可选，对象回收复用
                versionMsg.recycle()
            }
    ```
  * 手动管理生命周期
    ```kotlin
    val disposable = NFU.checkVersion().subscribe { }
    //取消任务和订阅
    disposable.dispose()
    ```

* *`preDownload()`* **同上**

* *`startUpgrade(ip: String)`* **同上**

### 4. 接收消息解析
* *`VersionMsg：`* 版本信息消息
  > **`state`** 状态

    |VersionState|含义|
    | ---- | ---- |
    |start|开始检测|
    |error|发生错误|
    |end|结束|
    |illegal|无效|

  > **`newer`** 是否有新版本

  > **`detail`** 版本信息或错误描述等

* *`DownloadMsg：`* 预下载消息
  > **`state`** 状态

    |DownloadState|含义|
    | ---- | ---- |
    |start|开始下载|
    |downloading|下载中|
    |error|发生错误|
    |end|结束|
    |illegal|无效|

  > **`progress`** 下载进度

  > **`detail`** 错误描述

* *`UpgradeMsg：`* 近场升级消息
  > **`state`** 状态

    |UpgradeState|含义|
    | ---- | ---- |
    |start|开始升级|
    |connecting|正在连接|
    |connectError|连接错误|
    |connected|已连接|
    |downloading|下载中|
    |downloadError|下载错误|
    |downloaded|下载完成|
    |transmitting|传输中|
    |transmitted|传输完成|
    |offline|掉线|
    |end|结束|
    |illegal|无效|

  > **`downloadProgress`** 下载进度

  > **`transmitProgress`** 传输进度

  > **`detail`** 错误异常等描述
