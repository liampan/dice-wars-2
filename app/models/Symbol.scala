package models

object Symbol {

  implicit val ord: Ordering[Symbol] = Ordering.by(_.order)

  case object Success extends Symbol {
    override def opposite: Option[Symbol] = Some(Failure)
    protected val order: Int = 1
  }

  case object Failure extends Symbol {
    override def opposite: Option[Symbol] = Some(Success)
    protected val order: Int = 2
  }

  case object Advantage extends Symbol {
    override def opposite: Option[Symbol] = Some(Threat)
    protected val order: Int = 3
  }

  case object Threat extends Symbol {
    override def opposite: Option[Symbol] = Some(Advantage)
    protected val order: Int = 4
  }

  case object Triumph extends Symbol {
    override def opposite: Option[Symbol] = None
    protected val order: Int = 5
  }

  case object Despair extends Symbol {
    override def opposite: Option[Symbol] = None
    protected val order: Int = 6
  }

}
sealed trait Symbol {
  def opposite: Option[Symbol]
  protected val order: Int
  def toLowerCase: String = this.toString.toLowerCase
}
