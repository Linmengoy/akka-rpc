package mr.linmeng.rpcfwk

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.concurrent.duration._

class Master extends  Actor{

  //存储worker信息的hashMap
  private val idToWorkerInfo = new mutable.HashMap[String, WorkerInfo]()

  override def preStart(): Unit = {

    //4. 收到心跳后开始定期检查异常worker节点
    import context.dispatcher
    context.system.scheduler.schedule(0 millisecond,20000 millisecond,self,CheckHeartbeat)
  }

  override def receive: Receive = {

    case RegisterWorker(workerId,memory,cores) => {
      // 5. master接收到信息，向worker返回注册成功信息
      //将Worker的信息封装起来，然后保存
      val workerInfo = new WorkerInfo(workerId,memory,cores)
      idToWorkerInfo(workerId) = workerInfo
      //向worker发送注册成功消息
      sender() ! RegisteredWorker
    }

    case Heartbeat(workerId) =>{
      //接收到worker后，如果已经注册，则更新上一次建立连接的事件
      if(idToWorkerInfo.contains(workerId)){
        val workerInfo = idToWorkerInfo(workerId)
        workerInfo.lastHeartbeatTime = System.currentTimeMillis()
      }
    }

    case CheckHeartbeat => {
      //定期检查
      val deadWorkers = idToWorkerInfo.values.filter(w => System.currentTimeMillis() - w.lastHeartbeatTime > 20000)
      deadWorkers.foreach(worker => {
        idToWorkerInfo -= worker.workId
      })
      println(s"current alive worker size: ${idToWorkerInfo.size}")
    }
  }
}

object Master {

  val MASTER_ACTOR_SYSTEM_NAME = "MasterActorSystem"
  val MASTER_ACTOR_NAME = "MasterActor"
  val CHECK_HEARTBEAT_INTERVAL = 20000

  def main(args: Array[String]): Unit = {
    //2. 创建一个master实例
    val hostname = args(0)
    val port = args(1).toInt
    //akka里面借鉴了一些netty的东西
    //参数的含义
    //akka.actor.provider 远程通信的实现方式
    //akka.remote.netty.tcp.hostname 绑定的地址
    //akka.remote.netty.tcp.port 绑定的端口
    val configStr =
    s"""
       |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
       |akka.remote.netty.tcp.hostname = $hostname
       |akka.remote.netty.tcp.port = $port
       |""".stripMargin

    val config = ConfigFactory.parseString(configStr)
    //创建ActorSystem
    val actorSystem = ActorSystem(MASTER_ACTOR_SYSTEM_NAME, config)
    //创建Actor实例
    actorSystem.actorOf(Props[Master],MASTER_ACTOR_NAME)

  }
}
