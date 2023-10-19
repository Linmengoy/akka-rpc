package mr.linmeng.rpc

case class RegisterWorker(
                         workerId:String,
                         memory:Int,
                         cores:Int
                         )

case object RegisteredWorker


case object SendHeartBeat


//之所以是多例的，因为他的worker不同
case class HeartBeat(workerId:String)