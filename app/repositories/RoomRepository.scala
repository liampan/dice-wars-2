package repositories

import akka.actor.ActorRef

trait RoomRepository {
  def getRoom(roomId: String): Room

  def addToRoom(room: String, af: ActorRef): Unit

  def leaveAllRooms(af: ActorRef): Unit

}

case class Room(participants: Set[ActorRef], messages: Seq[String] = Seq.empty) {
  def addParticipant(p: ActorRef) = this.copy(participants = participants + p)
  def removeParticipant(p: ActorRef) = this.copy(participants - p)
  def addMsg(msg: String) = this.copy(messages = messages :+ msg)
}

object Room {
  def empty = Room(Set.empty)
}

object RoomRepository extends RoomRepository {

  //room id -> participants
  private var rooms: Map[String, Room] = Map.empty.withDefault(_ => Room(Set.empty))

  def getRoom(roomId: String): Room = {
    cleanUp
    rooms.getOrElse(roomId, Room.empty)
  }

  def updateRoom(roomId: String, room: Room) = rooms = rooms.updated(roomId, room)

  def addMsg(roomId: String, msg: String) = {
    val room = getRoom(roomId).addMsg(msg)
    updateRoom(roomId, room)
    println(rooms)
    room
  }

  def addToRoom(roomId: String, af: ActorRef) =
    rooms = rooms.updated(roomId, getRoom(roomId).addParticipant(af))

  def leaveAllRooms(af: ActorRef) =
    rooms = rooms.map{case (key, room) => key -> room.removeParticipant(af)}

  def cleanUp =
    rooms = rooms.filter{case (_, room) => room.participants.nonEmpty }

}
