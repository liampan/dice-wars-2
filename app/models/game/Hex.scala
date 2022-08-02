package models.game

//       row,column
//  |  0,0  |  0,1  |  0,2  |     /row 0
//    \   /   \   /   \    /
//      |  1,0  |  1,1  |  1,2  |     /row 1
//    /   \   /   \   /   \   /
//  |  2,0  |  2,1  |  2,2  |        /row 2


final case class Hex(row: Int, column: Int) {

  def topPx = ((row * 22)/16.5) + 0.5
  def leftPx = (column * 26 + (if (evenRow) 0 else 13))/16.5

  private def evenRow: Boolean = row % 2 == 0

  def left: Hex = Hex(row, column -1)
  def upleft: Hex = if (evenRow) Hex(row-1, column -1) else Hex(row -1, column)
  def upright: Hex = if (evenRow) Hex(row-1, column) else Hex(row-1, column + 1)
  def right: Hex = Hex(row, column + 1)
  def downright: Hex = if (evenRow) Hex(row+1, column) else Hex(row + 1, column + 1)
  def downleft: Hex = if (evenRow) Hex(row+1, column-1) else Hex(row+1, column)


  def potentialNeighbors: Set[Hex] = Set(
    upleft, upright,
    left, right,
    downleft, downright
  )

  def confirmedNeighbors(availableHexes: Set[Hex]): Set[Hex] =
    potentialNeighbors.intersect(availableHexes)

  //      ^
  //  2 /   \ 3
  // 1 |     | 4
  //  6 \   /  5
  //      v

  def needsBorder(ownterritoryHexes: Set[Hex]) =
    Seq(
      1 -> left, 2 -> upleft,3 -> upright,4 -> right, 5 -> downright, 6 -> downleft
    ).filterNot{case (_, hex) => ownterritoryHexes.contains(hex)}.map(_._1)

}

