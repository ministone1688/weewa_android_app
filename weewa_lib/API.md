# Windows快传库API文档

快传库依赖于Qt 5.15或以上版本, 包含两个dll: `libsodium.dll`和`weewa_lib.dll`, 主入口类是`WeewaLib`

## WeewaLib

类`WeewaLib`为主要API入口, 单例模式

方法 | 说明
------------- | -------------
shared | 返回WeewaLib单例
launch  | 启动WeewaLib, 必须调用此api以便完成WeewaLib初始化
setRemoteDirectory | 设置远端目录, 如果你是发送文件, 则发送的文件将被保存到你指定的远端目录. 如果你是接收文件, 则接受文件的路径(相对路径的情况下)是相对于你设置的远端目录. 设置为空字符串则远端将会使用它的默认目录.
send | 向指定节点发送文件, 如果目标节点不在线, 则会等待<br> 参数: <br>* dstIp: 目标节点的ip<br>* dstName: 目标节点的名称, ip和名称至少要指定一个以便能匹配节点<br>* files: 需要传送的文件路径, 必须是绝对路径, 如果对应的文件找不到则会忽略, 如果所有的文件都找不到, 则传送不会继续.<br>* encrypt: 是否打开文件加密, 默认是false<br>返回:<br>传输会话的id
receive | 从指定节点接收文件, 如果指点的源节点不在线, 则会等待<br> 参数: <br>* srcIp: 源节点的ip<br>* srcName: 源节点的名称, ip和名称至少要指定一个以便能匹配节点<br>* files: 需要接收的文件, 支持绝对和相对路径, 如果是相对路径, 则都是相对于源节点的文件存储根目录来说的<br>* encrypt: 是否打开文件加密, 默认是false<br>返回:<br>传输会话的id
stop | 停止传输, 已经传输完的内容不会删除, 下次继续时会从断点开始<br> 参数: <br>* sessionId: 要停止的传输会话id

文件传输的整个过程会触发若干事件, 事件基于Qt的signal/slot机制, 目前会触发的signal有:

信号 | 说明
------------- | -------------
sessionWaiting | 由于目标或者源不在线, 传输会话在等待
sessionConnecting | 传输会话正在连接中 
sessionStarted | 传输会话已连接, 正在开始传输
sessionProgress | 传输中, 附带的进度值从0到1, 表示总进度
sessionEnded | 传输会话所有文件传输完成, 会话已结束
sessionError | 传输会话出错, 会话已结束

## 示例代码

```
#include "weewa_lib.h"
#include <QCoreApplication>
#include <QFile>
#include <QList>

int main(int argc, char *argv[]) {
    QCoreApplication app(argc, argv);
    
    QList<QString> files;
    files.append("/root/x86_64/system.img");
    QList<QString> recvFiles;
    recvFiles.append("NTUSER.DAT");

    // launch landrop
    WeewaLib::shared()->launch();
    WeewaLib::connect(WeewaLib::shared(), &WeewaLib::sessionWaiting, [](int id) {
        qDebug("session waiting: %d", id);
    });
    WeewaLib::connect(WeewaLib::shared(), &WeewaLib::sessionConnecting, [](int id) {
        qDebug("session connecting: %d", id);
    });
    WeewaLib::connect(WeewaLib::shared(), &WeewaLib::sessionStarted, [](int id) {
        qDebug("session started: %d", id);
    });
    WeewaLib::connect(WeewaLib::shared(), &WeewaLib::sessionProgress, [](int id, double progress) {
        qDebug("session %d progress: %.02f%%", id, progress * 100);
    });
    // WeewaLib::shared()->send(dstIp, dstName, files);
    // WeewaLib::shared()->receive("192.168.31.185", "", recvFiles);

    return app.exec();
}
```