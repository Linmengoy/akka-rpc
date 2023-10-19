package mr.linmeng.rpc

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable

class Master extends Actor{

  val idToWorkerInfo = new mutable.HashMap[String,WorkerInfo]()

  //receive用于接收其他Actor（也包括自己）发送的消息
  //type Receive = PartialFunction[Any , Unit] 偏函数，传入任意值，无法返回值
  override def receive: Receive = {
    case "today is sunny ~" => println("林蒙向你问好")

    case RegisterWorker(workerId,memory,cores) => {

      //将worker发送的消息保存起来
      val workerInfo = new WorkerInfo(workerId,memory,cores)
      //保存到可变的hashMap中
      idToWorkerInfo(workerId) = workerInfo
      //向worker返回注册成功的消息
      sender() ! RegisteredWorker

    }

    case "worker register" => { println("worker注册成功！")


      //master 接收到worker的消息后，给worker返回消息
      //获取消费发送者的引用 sender直接返回给发送位置消息
      sender() ! "ok"

    }

    case HeartBeat(workerId) => {
      println(s"work: $workerId 发送了心跳")
    }
    case _ => println("hi ~~~")
  }
}
object Master {
  def main(args: Array[String]): Unit = {
    val hostname = "localhost"
    val port = 8888

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
    val config: Config = ConfigFactory.parseString(configStr)

    //1. 调用ActorSystem的apply方法，传入ActorSystem的名称和配置信息
    val actorSystem: ActorSystem = ActorSystem.apply("MasterActorSystem", config)

    //2.创建Actor(使用反射创建的指定Actor类型的实例)
    //调用actorSystem的actorOf方法创建Actor，传入Actor的类型，Actor的名称
    val masterActor: ActorRef = actorSystem.actorOf(Props[Master], "MasterActor")

    //3.调用方法发消息（异步消息）
    //给指定的Actor发消息，！是一个方法
    masterActor ! "today is sunny ~"
  }
}
