package mr.linmeng.rpc

case class WorkerInfo(workerId:String , memory:Int , core:Int) {

  var lastHeartbeatTime :Long = _
}
