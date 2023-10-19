package mr.linmeng.rpc

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

class Worker extends Actor{
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


