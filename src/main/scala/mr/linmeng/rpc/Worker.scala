package mr.linmeng.rpc

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import java.util.UUID
//导入时间单位
import scala.concurrent.duration._

class Worker extends Actor{
  var masterRef: ActorSelection  = _

  //生成唯一的workerId
  val WORKER_ID = UUID.randomUUID().toString

  //Actor的声明周期方法，在构造方法之后，receive方法执行之前，一定会调用一次preStart方法
  override def preStart(): Unit = {
    //跟master建立连接，发送消息
    //actor有一个属性叫上下文（隐式参数），可以直接调用
    //master启动后会有一个listens地址，这个地址就是actor选择器所指向的地址
    // 后面的第一个路径/user表示为一个用户，固定参数
    // 第二个路径/MasterActor为master的actor名称
    //akka.tcp://${MasterActorSystemName}@${masterHostName}:${MasterPort}/user/${MasterActorName}
    //返回了与Master建立的连接（代理对象）
    masterRef =  context.actorSelection("akka.tcp://MasterActorSystem@localhost:8888/user/MasterActor")

    //向master发送消息
    masterRef ! RegisterWorker("worker01",10240,4)


  }

  override def receive: Receive = {

    case "hello" => println("hello c ~")

    case "ok" => println("master 返回消息 ")

    case RegisteredWorker => {
      //启动定时器，定期发送心跳
      //schedule定期调用指定的方法

      //导入隐式转换
      import context.dispatcher
      // 先给自己发消息
      context.system.scheduler.schedule(0 millisecond , 15000 millisecond ,self,SendHeartBeat)


      println("worker注册成功")
    }

    //在发送心跳前，可以实现一些逻辑判断
    case SendHeartBeat => {
      masterRef ! HeartBeat(WORKER_ID)

    }

    case _ => println("other message ~")
  }
}

object Worker {
  def main(args: Array[String]): Unit = {
    val hostname = "localhost"
    val port = 9998

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


