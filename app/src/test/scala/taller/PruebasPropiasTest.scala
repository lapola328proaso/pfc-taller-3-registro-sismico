package taller

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PruebasPropiasTest extends AnyFunSuite {

  val s = new RegistroSismico()
  import s._

  val vacio: List[Sismo] = List()

  val otro: List[Sismo] = List(
    Sismo(Fecha(2024, 1), "Cauca", 4.5, 12),
    Sismo(Fecha(2024, 1), "Narino", 4.0, 90),
    Sismo(Fecha(2024, 2), "Cauca", 6.5, 15),
    Sismo(Fecha(2024, 2), "Huila", 5.5, 70),
    Sismo(Fecha(2024, 3), "Narino", 5.0, 80),
    Sismo(Fecha(2024, 3), "Huila", 3.5, 60),
    Sismo(Fecha(2024, 4), "Cauca", 4.0, 10))

  // Punto 1 -------------------------------------------------------------------

  test("fuertes: umbral 4.5 deja tres regiones en orden de aparición") {
    assert(fuertes(otro, 4.5) == List("Cauca", "Huila", "Narino"))
  }

  test("fuertes: umbral 0 devuelve todas las regiones con repetidos") {
    assert(fuertes(otro, 0.0) ==
      List("Cauca", "Narino", "Cauca", "Huila", "Narino", "Huila", "Cauca"))
  }

  test("fuertes: sobre el registro vacío no hay regiones") {
    assert(fuertes(vacio, 1.0) == List())
  }

  test("enRegion: conserva el orden del registro") {
    assert(enRegion(otro, "Cauca") == List(4.5, 6.5, 4.0))
  }

  test("enRegion: distingue mayúsculas de minúsculas") {
    assert(enRegion(otro, "cauca") == List())
  }

  test("paresCercanos: con 0.0 solo empatan Cauca y Narino (4.0 y 4.0)") {
    assert(paresCercanos(otro, 0.0) == List(("Cauca", "Narino")))
  }

  test("paresCercanos: la diferencia es inclusiva, con 0.5 salen las tres parejas") {
    assert(paresCercanos(otro, 0.5) ==
      List(("Cauca", "Narino"), ("Cauca", "Huila"), ("Narino", "Huila")))
  }

  test("paresCercanos: la pareja sigue el orden de aparición, no el alfabético") {
    val ps = paresCercanos(otro, 0.5)
    assert(ps.contains(("Narino", "Huila")) && !ps.contains(("Huila", "Narino")))
  }

  test("paresCercanos: una sola región no forma parejas") {
    val uno = List(
      Sismo(Fecha(2024, 1), "Cauca", 4.0, 10),
      Sismo(Fecha(2024, 2), "Cauca", 4.0, 10))
    assert(paresCercanos(uno, 10.0) == List())
  }

  test("paresCercanos: sobre el registro vacío no hay parejas") {
    assert(paresCercanos(vacio, 1.0) == List())
  }

  // Punto 2 -------------------------------------------------------------------

  test("conteoPorRegion: tres, dos y dos en el otro registro") {
    assert(conteoPorRegion(otro) == Map("Cauca" -> 3, "Narino" -> 2, "Huila" -> 2))
  }

  test("conteoPorRegion: la suma de los conteos es el tamaño del registro") {
    assert(conteoPorRegion(otro).values.sum == otro.length)
  }

  test("magnitudMaxima: la mayor de cada región") {
    assert(magnitudMaxima(otro) ==
      Map("Cauca" -> 6.5, "Narino" -> 5.0, "Huila" -> 5.5))
  }

  test("magnitudMaxima: un solo sismo es su propio máximo") {
    assert(magnitudMaxima(List(Sismo(Fecha(2024, 1), "Cauca", 4.0, 10))) ==
      Map("Cauca" -> 4.0))
  }

  test("porRegion: conserva el orden de los sismos dentro de cada región") {
    assert(porRegion(otro)("Narino").map(_.magnitud) == List(4.0, 5.0))
  }

  test("porRegion: no pierde ni inventa sismos") {
    assert(porRegion(otro).values.flatten.toSet == otro.toSet)
  }

  test("porRegion: el registro vacío da el mapa vacío") {
    assert(porRegion(vacio) == Map())
  }

  test("agrupamientos: no dependen del orden del registro") {
    assert(conteoPorRegion(otro.reverse) == conteoPorRegion(otro))
    assert(magnitudMaxima(otro.reverse) == magnitudMaxima(otro))
  }

  // Punto 3 -------------------------------------------------------------------

  test("conteoSeguro: una región conocida responde su conteo") {
    assert(conteoSeguro(otro)("Narino") == 2)
  }

  test("conteoSeguro: varias regiones desconocidas responden cero") {
    assert(conteoSeguro(otro)("Amazonas") == 0)
    assert(conteoSeguro(otro)("Quindio") == 0)
  }

  test("conteoSeguro: el valor por defecto no agrega llaves") {
    assert(!conteoSeguro(otro).contains("Amazonas"))
    assert(conteoSeguro(otro).size == 3)
  }

  test("maximaDe: máximos de regiones del otro registro") {
    assert(maximaDe(otro, "Cauca") == Some(6.5))
    assert(maximaDe(otro, "Huila") == Some(5.5))
  }

  test("maximaDe: región ausente o registro vacío dan None") {
    assert(maximaDe(otro, "Amazonas") == None)
    assert(maximaDe(vacio, "Cauca") == None)
  }

  test("regionMasActiva: Cauca tiene tres sismos") {
    assert(regionMasActiva(otro) == Some("Cauca"))
  }

  test("regionMasActiva: en empate gana la primera alfabéticamente, no la primera en aparecer") {
    val empate = List(
      Sismo(Fecha(2024, 1), "Narino", 4.0, 10),
      Sismo(Fecha(2024, 1), "Huila", 4.0, 10),
      Sismo(Fecha(2024, 2), "Narino", 4.0, 10),
      Sismo(Fecha(2024, 2), "Huila", 4.0, 10),
      Sismo(Fecha(2024, 3), "Cauca", 4.0, 10))
    assert(regionMasActiva(empate) == Some("Huila"))
  }

  test("regionMasActiva: con una sola región devuelve esa región") {
    assert(regionMasActiva(List(Sismo(Fecha(2024, 1), "Cauca", 4.0, 10))) ==
      Some("Cauca"))
  }

  // Punto 4 -------------------------------------------------------------------

  test("replicas: las primeras tres de un sismo de 10.0") {
    assert(replicas(10.0).take(3).toList == List(10.0, 8.0, 6.4))
  }

  test("replicas: acceso por índice con redondeo a dos decimales") {
    assert(replicas(6.0)(2) == 3.84)
    assert(replicas(6.0)(3) == 3.07)
  }

  test("replicas: take(0) es vacío y no calcula nada") {
    assert(replicas(5.0).take(0).toList == List())
  }

  test("replicas: una magnitud cero produce ceros") {
    assert(replicas(0.0).take(3).toList == List(0.0, 0.0, 0.0))
  }

  test("replicas: diez elementos y estrictamente decrecientes") {
    val xs = replicas(5.0).take(10).toList
    assert(xs.length == 10)
    assert(xs.zip(xs.tail).forall { case (a, b) => b < a })
  }

  test("replicasSensibles: de 10.0 con mínimo 6.0") {
    assert(replicasSensibles(10.0, 6.0) == List(10.0, 8.0, 6.4))
  }

  test("replicasSensibles: el mínimo excluye al valor igual") {
    assert(replicasSensibles(5.0, 4.0) == List(5.0))
  }

  test("replicasSensibles: con mínimo cero termina por el redondeo") {
    assert(replicasSensibles(5.0, 0.0).length == 31)
  }

  test("replicasSensibles: de 4.0 con mínimo 3.0") {
    assert(replicasSensibles(4.0, 3.0) == List(4.0, 3.2))
  }

  // Punto 5 -------------------------------------------------------------------

  test("informe: resumen de las tres regiones del otro registro") {
    assert(informe(otro, 3.0) ==
      Map("Cauca" -> Resumen(3, 6.5, 4),
        "Narino" -> Resumen(2, 5.0, 3),
        "Huila" -> Resumen(2, 5.5, 3)))
  }

  test("informe: con mínimo 6.0 solo Cauca tiene réplicas, las demás regiones siguen apareciendo") {
    assert(informe(otro, 6.0) ==
      Map("Cauca" -> Resumen(3, 6.5, 1),
        "Narino" -> Resumen(2, 5.0, 0),
        "Huila" -> Resumen(2, 5.5, 0)))
  }

  test("informe: con un mínimo muy alto ninguna región tiene réplicas") {
    assert(informe(otro, 10.0).values.forall(_.replicas == 0))
  }

  test("informe: una región con un solo sismo") {
    assert(informe(List(Sismo(Fecha(2024, 1), "Cauca", 4.0, 10)), 3.0) ==
      Map("Cauca" -> Resumen(1, 4.0, 2)))
  }

  test("informe: las réplicas salen del sismo más fuerte de la región") {
    val r = informe(otro, 3.0)("Cauca")
    assert(r.maxima == 6.5)
    assert(r.replicas == replicasSensibles(6.5, 3.0).length)
    assert(r.replicas != replicasSensibles(4.0, 3.0).length)
  }

  test("informe: no depende del orden del registro") {
    assert(informe(otro.reverse, 3.0) == informe(otro, 3.0))
  }
}
