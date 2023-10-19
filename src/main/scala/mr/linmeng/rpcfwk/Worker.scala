package mr.linmeng.rpcfwk

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import java.util.UUID
import scala.concurrent.duration._

class Worker(val masterHost: String,
             val masterPort: Int,
             var memory: Int,
             var cores: Int) extends Actor {

  var masterRef: ActorSelection = _
  //一般workerID由配置文件，使用者自己定义
  val WORKER_ID = UUID.randomUUID().toString
  override def preStart(): Unit = {
    // 3. worker向master发送注册消息
    // 与master建立连接
    masterRef = context.actorSelection(s"akka://${Master.MASTER_ACTOR_SYSTEM_NAME}@${masterHost}:${masterPort}/user/${Master.MASTER_ACTOR_NAME}")
    //向master发送注册消息
    masterRef ! RegisterWorker(WORKER_ID, memory, cores)
  }

  override def receive: Receive = {

    case RegisteredWorker => {
      // 6. master返回消息后，worker启动定时发送心跳
      //worker收到注册成功消息，开始启动定时，定期发送心跳
      import context.dispatcher
      //15秒发送一次心跳,发送给自己，在另一个case中做逻辑处理
      context.system.scheduler.schedule(0 millisecond,15000 millisecond , self,SendHeartbeat)
    }
    case SendHeartbeat => {
      //一些逻辑处理操作
      //向master发送心跳
      masterRef ! Heartbeat(WORKER_ID)
    }

  }
}

object Worker {

  val WORKER_ACTOR_SYSTEM_NAME = "WorkerActorSystem"
  val WORKER_ACTOR_NAME = "WorkerActor"

  def main(args: Array[String]): Unit = {
    //1. 创建一个worker实例
    //master的连接
    val masterHost = args(0)
    val masterPort = args(1).toInt
    //当前worker连接
    val workerHost = args(2)
    val workerPort = args(3).toInt
    //当前worker状态
    val workerMemory = args(4).toInt
    val workerCores = args(5).toInt
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = $workerHost
         |akka.remote.netty.tcp.port = $workerPort
         |""".stripMargin

    val config = ConfigFactory.parseString(configStr)
    //创建ActorSystem
    val actorSystem = ActorSystem(WORKER_ACTOR_SYSTEM_NAME, config)
    //创建Actor实例
    actorSystem.actorOf(Props(new Worker(masterHost, masterPort, workerMemory, workerCores)), WORKER_ACTOR_NAME)

  }

}
