package mr.linmeng.rpc

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

class Worker extends Actor{

  //Actor的声明周期方法，在构造方法之后，receive方法执行之前，一定会调用一次preStart方法
  override def preStart(): Unit = {
    //跟master建立连接，发送消息
    //actor有一个属性叫上下文（隐式参数），可以直接调用
    //master启动后会有一个listens地址，这个地址就是actor选择器所指向的地址
    // 后面的第一个路径/user表示为一个用户，固定参数
    // 第二个路径/MasterActor为master的actor名称
    //akka.tcp://${MasterActorSystemName}@${masterHostName}:${MasterPort}/user/${MasterActorName}
    //返回了与Master建立的连接（代理对象）
     val masterRef =  context.actorSelection("akka.tcp://MasterActorSystem@localhost:8888/user/MasterActor")

    masterRef ! "worker register"


  }

  override def receive: Receive = {

    case "hello" => println("hello c ~")

    case _ => println("other message ~")
  }
}

object Worker {
  def main(args: Array[String]): Unit = {
    val hostname = "localhost"
    val port = "9999"

    val configStr =
      s"""
        |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
        |akka.remote.netty.tcp.hostname = $hostname
        |akka.remote.netty.tcp.port = $port
        |""".stripMargin

    val config = ConfigFactory.parseString(configStr)

    val workerActor = ActorSystem("WorkerActorSystem", config).actorOf(Props[Worker], "WorkerActor")

    workerActor ! "hello"


  }

}


