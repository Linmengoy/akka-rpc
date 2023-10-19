package mr.linmeng.rpcfwk

//worker发送注册实例
case class RegisterWorker(workId:String , memory:Int , cores: Int)

//master返回注册成功消息
case object RegisteredWorker


case class WorkerInfo(workId:String , memory:Int , cores: Int){
  var lastHeartbeatTime: Long = _
}

case object SendHeartbeat

//向master发送心跳的worker节点标记
case class Heartbeat(workId:String)


//master检查自身心跳
case object  CheckHeartbeat