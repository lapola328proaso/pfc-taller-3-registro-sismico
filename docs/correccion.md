# Informe de corrección — Taller 3: consultas sobre un registro sísmico

## Notación

- $R = [s_1, \dots, s_n]$ es el registro; $[\,]$ es el registro vacío y $s :: R'$ el registro con $s$ al frente de $R'$.
- Para un sismo $s$: $s.r$ es su región y $s.m$ su magnitud.
- $R_g = [\,s \in R \mid s.r = g\,]$ es la sublista de $R$ con región $g$, en el orden de $R$.
- $\rho(R) = \{\, s.r \mid s \in R \,\}$ es el conjunto de regiones de $R$.
- $\mathrm{round}_2(x) = \lfloor 100x + 0.5 \rfloor / 100$.

Las pruebas se hacen por inducción estructural sobre la lista $R$: el caso base es $R = [\,]$ y el paso agrega un sismo al frente. Las pruebas de la secuencia perezosa se enuncian sobre los primeros $n$ elementos.

## 1. Punto 1

### `fuertes`

**Especificación.** $\mathrm{fuertes}(R, u) = [\, s.r \mid s \in R,\ s.m > u \,]$, conservando el orden de $R$.

La expresión `for (s <- r if s.magnitud > umbral) yield s.region` equivale a `r.withFilter(...).map(...)`, y para una lista `s :: R'` ambas operaciones cumplen:

$$
\mathrm{fuertes}(s :: R', u) =
\begin{cases}
s.r :: \mathrm{fuertes}(R', u) & \text{si } s.m > u \\
\mathrm{fuertes}(R', u) & \text{si no}
\end{cases}
$$

- **Caso base:** $R = [\,]$. `withFilter` y `map` sobre la lista vacía devuelven $[\,]$, que es la especificación.
- **Paso:** por hipótesis de inducción (HI), $\mathrm{fuertes}(R', u)$ cumple la especificación sobre $R'$. Si $s.m > u$, el resultado es $s.r$ seguido de las regiones fuertes de $R'$, que es la especificación para $s :: R'$; si no, $s$ no aporta y el resultado es el de $R'$.

Como el umbral se compara con $>$, un sismo con $s.m = u$ no entra. No hay `distinct`, así que los repetidos se conservan.

### `enRegion`

**Especificación.** $\mathrm{enRegion}(R, g) = [\, s.m \mid s \in R,\ s.r = g \,]$.

La prueba es idéntica a la de `fuertes` cambiando el filtro $s.m > u$ por $s.r = g$. Si $g \notin \rho(R)$, ningún $s$ pasa el filtro y el resultado es $[\,]$.

### `paresCercanos`

**Especificación.** Sea $[g_0, g_1, \dots, g_{k-1}]$ la lista de las regiones de $\rho(R)$, sin repetir y en orden de primera aparición, y

$$
C(a, b) \iff \exists\, x, y \in R :\ x.r = a \ \wedge\ y.r = b \ \wedge\ |x.m - y.m| \le d .
$$

Entonces

$$
\mathrm{paresCercanos}(R, d) = [\, (g_i, g_j) \mid 0 \le i < j < k,\ C(g_i, g_j) \,]
$$

ordenada lexicográficamente por $(i, j)$.

**`hayCercanos(a, b)` $\iff C(a, b)$.** El `for` interior produce un `()` por cada pareja $(x, y) \in R \times R$ con $x.r = a$, $y.r = b$ y $|x.m - y.m| \le d$ (por inducción sobre $R$ en el generador exterior y luego en el interior, igual que en `fuertes`). La lista resultante es no vacía exactamente cuando existe esa pareja, es decir, cuando $C(a, b)$.

**Corrección del `for` exterior.**

1. *Regiones distintas:* `indexadas` no tiene repetidos, por lo que $i < j$ implica $g_i \ne g_j$.
2. *Cada pareja una sola vez:* como $|x.m - y.m| = |y.m - x.m|$, $C(a, b) \iff C(b, a)$. Entre $(g_i, g_j)$ y $(g_j, g_i)$ el filtro $i < j$ deja exactamente una, así que ninguna pareja se pierde ni se duplica.
3. *Orden:* `flatMap` recorre $i$ en el exterior y $j$ en el interior, ambos en orden ascendente, lo que da el orden lexicográfico por $(i, j)$, que es el orden de aparición.

**Límite.** La comparación $|x.m - y.m| \le d$ se evalúa con aritmética de `Double`. Por ejemplo, $|4.2 - 3.6|$ se evalúa como $0.6000000000000001$, así que con $d = 0.6$ esa pareja no entra aunque en decimales exactos la diferencia sea $0.6$. La corrección se afirma respecto de los valores `Double` del registro.

## 2. Punto 2

**Especificación.** Para toda región $g \in \rho(R)$:

$$
\mathrm{porRegion}(R)(g) = R_g, \qquad
\mathrm{conteoPorRegion}(R)(g) = |R_g|, \qquad
\mathrm{magnitudMaxima}(R)(g) = \max\{\, s.m \mid s \in R_g \,\},
$$

y el dominio de los tres mapas es exactamente $\rho(R)$.

- `groupBy(_.region)` crea una llave por cada valor distinto de `region` y asocia a cada una los elementos que la tienen; por tanto el dominio es $\rho(R)$ y el valor de $g$ es $R_g$.
- Cada $R_g$ con $g \in \rho(R)$ es no vacío (existe al menos un $s$ con $s.r = g$), de modo que `max` está definido.
- `conteoPorRegion` aplica `size` a cada $R_g$ y `magnitudMaxima` aplica `map(_.magnitud).max`, que por definición es el máximo de la especificación.
- Si $R = [\,]$, $\rho(R) = \emptyset$, así que los tres mapas son `Map()`. Una región fuera de $\rho(R)$ no es llave.

### El resultado no depende del orden de recorrido

Sea $\pi$ una permutación de $R$ y $R^\pi$ el registro reordenado.

1. **Dominio.** $\rho(R^\pi) = \rho(R)$, porque $\rho$ es un conjunto y una permutación no cambia qué elementos hay.
2. **Conteo.** $R^\pi_g$ es una permutación de $R_g$, y la cardinalidad no cambia al permutar: $|R^\pi_g| = |R_g|$.
3. **Máximo.** $\max$ es conmutativo ($\max(a, b) = \max(b, a)$), asociativo e idempotente, por lo que el máximo de un conjunto finito no depende del orden en que se combinan sus elementos: $\max R^\pi_g = \max R_g$.

Por tanto $\mathrm{conteoPorRegion}(R^\pi) = \mathrm{conteoPorRegion}(R)$ y $\mathrm{magnitudMaxima}(R^\pi) = \mathrm{magnitudMaxima}(R)$ como mapas, y los mapas no tienen orden de llaves que compare. Lo que sí depende del orden son las listas dentro de `porRegion` (el orden interno de cada $R_g$), y el orden de las salidas de `fuertes`, `enRegion` y `paresCercanos`, que la especificación define como orden de aparición. Como conjunto de parejas no ordenadas, `paresCercanos` tampoco cambia al permutar, porque $C(a, b)$ solo depende de los conjuntos de magnitudes de las dos regiones; lo que sí cambia es la orientación de cada pareja, que sigue el orden de aparición.

## 3. Punto 3

**`conteoSeguro`.** $\mathrm{conteoSeguro}(R)(g) = |R_g|$ para **todo** $g$. Si $g \in \rho(R)$, es el valor del mapa. Si $g \notin \rho(R)$, `withDefaultValue(0)` responde $0 = |[\,]| = |R_g|$. La llave no se agrega: `contains` y `size` no cambian.

**`maximaDe`.** $\mathrm{maximaDe}(R, g) = \mathrm{Some}(\max\{s.m \mid s \in R_g\})$ si $R_g \ne [\,]$, y $\mathrm{None}$ si $R_g = [\,]$. Por el punto 2, $g$ es llave de `magnitudMaxima(R)` si y solo si $R_g \ne [\,]$, y `get` devuelve `Some` exactamente en ese caso.

**`regionMasActiva`.** Sea $n_g = |R_g|$. Se busca

$$
g^* = \min_{\text{alfabético}} \{\, g \in \rho(R) \mid n_g = \max_{h \in \rho(R)} n_h \,\}.
$$

`sortBy` con la clave $(-n_g, g)$ ordena de forma creciente en el orden lexicográfico de pares. El primer elemento minimiza $-n_g$ (es decir, maximiza $n_g$) y, entre los de igual $n_g$, minimiza $g$. Las llaves del mapa son distintas, así que el mínimo es único. Si $R = [\,]$, la lista es vacía y `headOption` devuelve $\mathrm{None}$.

Como la entrada es un mapa independiente del orden de $R$ (punto 2), el resultado tampoco depende de ese orden: el desempate es alfabético, no por aparición.

## 4. Punto 4

### `replicas`

Sea $a_0 = m$ y $a_{k+1} = 0.8\, a_k$, de donde $a_k = m \cdot 0.8^k$.

**Propiedad $P(n)$:** para todo $k < n$, el elemento $k$ de $\mathrm{replicas}(m)$ es $\mathrm{round}_2(a_k)$.

`LazyList.iterate(m)(f)` es, por definición, la secuencia cuyo elemento $k$ es $f^k(m)$ (con $f(x) = 0.8x$) y `map(redondear)` aplica $\mathrm{round}_2$ elemento a elemento.

- **Caso base** ($k = 0$): el elemento 0 es $f^0(m) = m = a_0$, y su versión redondeada es $\mathrm{round}_2(a_0)$.
- **Paso:** si el elemento $k$ de `iterate` es $a_k$, el elemento $k + 1$ es $f(a_k) = 0.8\, a_k = a_{k+1}$.

La propiedad se enuncia sobre los primeros $n$ elementos porque la secuencia es infinita: cada elemento se calcula solo cuando se pide, y `take(n)` pide únicamente $n$ celdas, de modo que $P(n)$ es lo único que se necesita evaluar. La demostración usa aritmética real; la implementación trabaja con `Double`, por lo que cada valor difiere del real en un error de punto flotante muy pequeño. En los casos del enunciado ese error no cambia el redondeo a dos decimales (por ejemplo, $0.8^3 \cdot 5 = 2.56$).

El redondeo se aplica a cada elemento de la salida y no dentro de la iteración. Si se redondeara en cada paso, la sucesión $z_{k+1} = \mathrm{round}_2(0.8\, z_k)$ tendría puntos fijos distintos de cero, como $z = 0.01$ y $z = 0.02$, y `replicasSensibles(m, 0.0)` no terminaría.

### `replicasSensibles`

**Especificación.** $\mathrm{replicasSensibles}(m, \mu) = [\, \mathrm{round}_2(a_k) \mid k \ge 0,\ \mathrm{round}_2(a_k) > \mu \,]$ para $m \ge 0$, $\mu \ge 0$.

1. *Los elementos que cumplen forman un prefijo.* Con $m \ge 0$, $a_k$ es no creciente y $\mathrm{round}_2$ es monótona no decreciente. Si $\mathrm{round}_2(a_j) \le \mu$, entonces para $k > j$: $\mathrm{round}_2(a_k) \le \mathrm{round}_2(a_j) \le \mu$. Por eso `takeWhile` (que se detiene en el primer incumplimiento) devuelve **todos** los elementos que cumplen, igual que `filter` pero sin recorrer la cola infinita.
2. *Termina.* Como $a_k \to 0$, existe $K$ con $a_K < 0.005$, de modo que $\mathrm{round}_2(a_K) = 0 \le \mu$. La lista tiene a lo sumo $K$ elementos. Con $\mu < 0$ la condición no se rompería y `toList` no terminaría; el enunciado no contempla ese caso.
3. *Incluye la primera:* si $\mathrm{round}_2(a_0) > \mu$, el elemento 0 está en el resultado; si no, el resultado es $[\,]$, como en `replicasSensibles(2.0, 3.0)`.
4. *Memoria:* `takeWhile` fuerza a lo sumo $|\mathrm{resultado}| + 1$ celdas (la última, para comprobar que falla).

## 5. Punto 5

**Especificación.** Para cada $g \in \rho(R)$:

$$
\mathrm{informe}(R, \mu)(g) = \mathrm{Resumen}\big(\,|R_g|,\ M_g,\ |\mathrm{replicasSensibles}(M_g, \mu)|\,\big), \qquad M_g = \max\{\, s.m \mid s \in R_g \,\}.
$$

- El `for` itera sobre `magnitudMaxima(r)`, cuyo dominio es $\rho(R)$ (punto 2): hay una entrada por región y ninguna más.
- Para cada $(g, M_g)$, `conteo(g)` es $|R_g|$ (punto 2), `maxima` es $M_g$ y `replicasSensibles(M_g, mu).size` es el número de réplicas sensibles del sismo más fuerte de $g$ (punto 4).
- Si $R = [\,]$, el dominio es vacío y el resultado es `Map()`.

**Independencia del orden.** $\rho(R)$, $|R_g|$ y $M_g$ no cambian al permutar $R$ (punto 2), y $\mathrm{replicasSensibles}$ solo depende de $M_g$ y $\mu$. Por composición, $\mathrm{informe}(R^\pi, \mu) = \mathrm{informe}(R, \mu)$ para toda permutación $\pi$.