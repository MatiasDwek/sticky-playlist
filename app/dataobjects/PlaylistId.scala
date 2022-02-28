package dataobjects

// TODO merge playlist data and playlist id in the same file
object PlaylistId {
  def apply(raw: String): PlaylistId = {
    require(raw != null)
    new PlaylistId(raw)
  }
}

class PlaylistId private(val underlying: String) extends AnyVal {
  override def toString: String = underlying
}