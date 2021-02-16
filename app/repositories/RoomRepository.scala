package repositories

import akka.actor.ActorRef

object RoomRepository {

  //room id -> participants
  var rooms: Map[String, Set[ActorRef]] = Map.empty.withDefault(_ => Set.empty)

  def addToRoom(room: String, af: ActorRef) =
    rooms = rooms.updated(room, rooms.apply(room) + af)

  def leaveAllRooms(af: ActorRef) =
    rooms = rooms.map{case (key, users) => key -> (users - af)}

}
