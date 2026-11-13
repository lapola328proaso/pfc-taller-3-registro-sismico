package taller

class RegistroSismico {

  type Registro = List[Sismo]

  def fuertes(r: Registro, umbral: Double): List[String] =
    for (s <- r if s.magnitud > umbral) yield s.region

  def enRegion(r: Registro, region: String): List[Double] =
    for (s <- r if s.region == region) yield s.magnitud

  def paresCercanos(r: Registro, dif: Double): List[(String, String)] = {
    val indexadas = (for (s <- r) yield s.region).distinct.zipWithIndex

    def hayCercanos(a: String, b: String): Boolean =
      (for {
        x <- r if x.region == a
        y <- r if y.region == b
        if Math.abs(x.magnitud - y.magnitud) <= dif
      } yield ()).nonEmpty

    for {
      (a, i) <- indexadas
      (b, j) <- indexadas
      if i < j
      if hayCercanos(a, b)
    } yield (a, b)
  }

  def porRegion(r: Registro): Map[String, List[Sismo]] =
    r.groupBy(_.region)

  def conteoPorRegion(r: Registro): Map[String, Int] =
    porRegion(r).map { case (region, sismos) => region -> sismos.size }

  def magnitudMaxima(r: Registro): Map[String, Double] =
    porRegion(r).map { case (region, sismos) =>
      region -> sismos.map(_.magnitud).max
    }

  def conteoSeguro(r: Registro): Map[String, Int] =
    conteoPorRegion(r).withDefaultValue(0)

  def maximaDe(r: Registro, region: String): Option[Double] =
    magnitudMaxima(r).get(region)

  def regionMasActiva(r: Registro): Option[String] =
    conteoPorRegion(r).toList
      .sortBy { case (region, n) => (-n, region) }
      .headOption
      .map { case (region, _) => region }

  def replicas(magnitud: Double): LazyList[Double] = {
    def redondear(x: Double): Double = Math.round(x * 100.0) / 100.0
    LazyList.iterate(magnitud)(_ * 0.8).map(redondear)
  }

  def replicasSensibles(magnitud: Double, minimo: Double): List[Double] =
    replicas(magnitud).takeWhile(_ > minimo).toList

  def informe(r: Registro, minimo: Double): Map[String, Resumen] = {
    val conteo = conteoPorRegion(r)
    for ((region, maxima) <- magnitudMaxima(r)) yield {
      region -> Resumen(conteo(region), maxima, replicasSensibles(maxima, minimo).size)
    }
  }
}