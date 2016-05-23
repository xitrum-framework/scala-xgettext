package scala

import scala.tools.nsc.Global

/**
 * Adapter for hiding differences between major Scala versions
 */
trait ScalaVersionAdapter {
  protected val global: Global
  import global._

  protected final def getTypeFor(typeName: String) = {
    rootMirror.getClassByName(TypeName(typeName)).tpe
  }
}
