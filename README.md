
![img.png](img.png) analisis de Sonarqube

# Productos Service — Análisis SonarQube

## 1. Descripción del proyecto

Este repositorio corresponde al laboratorio de la Unidad 10: Métricas de Calidad y SonarQube. El objetivo principal de la actividad fue configurar un proyecto Spring Boot con código intencionalmente imperfecto, levantar un servidor local de SonarQube mediante Docker, integrar JaCoCo para la generación del reporte de cobertura y ejecutar un análisis estático inicial del código fuente.

El análisis permitió identificar problemas relacionados con mantenibilidad, confiabilidad y cobertura de pruebas, los cuales fueron registrados en el dashboard de SonarQube. En esta primera etapa no se realizaron correcciones sobre el código, ya que el propósito del laboratorio fue interpretar los hallazgos iniciales y documentarlos técnicamente.

---

## 2. Tecnologías utilizadas

- Java JDK 21
- Spring Boot
- Maven
- SonarQube Community Edition
- Docker Desktop
- JaCoCo
- H2 Database
- Lombok
- Git y GitHub

---

## 3. Configuración del análisis

Para ejecutar el análisis se levantó SonarQube localmente mediante Docker en el puerto `9000`. Posteriormente, se creó el proyecto manualmente desde el dashboard de SonarQube con la siguiente configuración:

```properties
sonar.projectKey=com.universidad:productos-service
sonar.projectName=Productos Service
sonar.projectVersion=1.0
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.exclusions=**/*Application.java
sonar.host.url=http://localhost:9000
sonar.qualitygate.wait=false

El análisis fue ejecutado con el siguiente comando:

mvn clean verify sonar:sonar -Dsonar.token=TU_TOKEN

El comando permitió compilar el proyecto, ejecutar las pruebas, generar el reporte de cobertura con JaCoCo y enviar los resultados al dashboard local de SonarQube.

4. Estado inicial del análisis
Categoría	Cantidad	Rating
Bugs	X	X
Vulnerabilidades	X	X
Code Smells	X	X
Cobertura	X%	—
Duplicaciones	X%	—
Security Hotspots	X	X

Nota: Los valores de esta tabla corresponden al primer análisis ejecutado sobre el proyecto antes de aplicar correcciones de calidad.

5. Hallazgos principales identificados
Bug 1: Retorno de valor nulo al buscar producto
Archivo: ProductoService.java
Método: buscar(Long id)
Línea: X
Categoría: Bug
Severidad: Major
Descripción:
El método buscar(Long id) retorna null cuando no encuentra un producto en el repositorio. Esta práctica puede generar errores en tiempo de ejecución, especialmente NullPointerException, si otra parte del sistema intenta utilizar el objeto retornado sin validar previamente si existe o no.
Código asociado:
public Producto buscar(Long id) {
    return repo.findById(id).orElse(null);
}
Análisis técnico:
Desde el punto de vista de calidad de software, retornar null reduce la confiabilidad del sistema porque obliga a los consumidores del método a realizar validaciones adicionales. Una alternativa más segura sería lanzar una excepción controlada o retornar un Optional<Producto>, dependiendo del diseño del servicio.
Code Smell 1: Inyección de dependencias mediante atributo
Archivo: ProductoService.java
Línea: X
Categoría: Code Smell
Severidad: Major
Descripción:
La clase ProductoService utiliza inyección de dependencias directamente sobre el atributo mediante @Autowired. Esta práctica disminuye la claridad del diseño y dificulta la escritura de pruebas unitarias, ya que la dependencia queda acoplada directamente al campo interno de la clase.
Código asociado:
@Autowired
private ProductoRepository repo;
Análisis técnico:
Una mejor práctica en Spring Boot es utilizar inyección por constructor, ya que permite declarar las dependencias obligatorias de forma explícita, facilita las pruebas y mejora la mantenibilidad del código.
Code Smell 2: Nombre genérico de variable
Archivo: ProductoService.java
Línea: X
Categoría: Code Smell
Severidad: Minor
Descripción:
La variable repo tiene un nombre demasiado genérico. Aunque el código compila correctamente, este tipo de nombres reduce la legibilidad, especialmente cuando el sistema crece y se incorporan más repositorios o dependencias.
Código asociado:
private ProductoRepository repo;
Análisis técnico:
El nombre de la variable debería expresar con precisión su responsabilidad dentro de la clase. En este caso, sería más adecuado utilizar productoRepository, ya que comunica explícitamente que la dependencia corresponde al repositorio de productos.
Code Smell 3: Método con múltiples responsabilidades
Archivo: ProductoService.java
Método: procesarProducto(...)
Línea: X
Categoría: Code Smell
Severidad: Major
Descripción:
El método procesarProducto concentra varias responsabilidades: validación del nombre, validación del precio, validación del stock, creación del objeto Producto y persistencia en base de datos. Esta concentración de lógica aumenta la complejidad ciclomática y dificulta la mantenibilidad del servicio.
Código asociado:
public Producto procesarProducto(String n, Double p, Integer s,
                                 String cat, boolean activo, String proveedor) {
    Producto producto = new Producto();

    if (n == null || n.equals("")) {
        throw new IllegalArgumentException("nombre requerido");
    }

    if (p == null) {
        throw new IllegalArgumentException("precio requerido");
    } else if (p <= 0) {
        throw new IllegalArgumentException("precio invalido");
    } else if (p > 999999) {
        throw new IllegalArgumentException("precio excesivo");
    }

    if (s == null || s < 0) {
        throw new IllegalArgumentException("stock invalido");
    }

    producto.setNombre(n);
    producto.setPrecio(p);
    producto.setStock(s);

    return repo.save(producto);
}
Análisis técnico:
El método debería dividirse en operaciones más específicas, por ejemplo: validar nombre, validar precio, validar stock, construir el producto y guardar el producto. Esta separación facilita las pruebas unitarias, reduce la complejidad y mejora la evolución del código.
Code Smell 4: Comparación manual de cadena vacía
Archivo: ProductoService.java
Línea: X
Categoría: Code Smell
Severidad: Minor
Descripción:
El código valida si el nombre está vacío usando n.equals(""). Esta validación no considera correctamente cadenas con espacios en blanco y es menos expresiva que el uso de métodos especializados como isBlank().
Código asociado:
if (n == null || n.equals("")) {
    throw new IllegalArgumentException("nombre requerido");
}
Análisis técnico:
Una validación más completa debería considerar valores nulos, cadenas vacías y cadenas compuestas únicamente por espacios. Por ello, una opción más clara sería utilizar n == null || n.isBlank().
Code Smell 5: Lógica de negocio dentro de entidad JPA
Archivo: Producto.java
Método: getEstado()
Línea: X
Categoría: Code Smell
Severidad: Major
Descripción:
La entidad Producto contiene lógica de negocio para determinar el estado del producto según el valor del stock. Aunque esta lógica funciona, puede considerarse un problema de diseño si la entidad comienza a asumir responsabilidades que deberían estar ubicadas en servicios de dominio o componentes especializados.
Código asociado:
public String getEstado() {
    if (stock == null) return "DESCONOCIDO";
    if (stock == 0) return "AGOTADO";
    if (stock > 0 && stock <= 5) return "BAJO";
    if (stock > 5 && stock <= 20) return "NORMAL";
    if (stock > 20 && stock <= 50) return "ALTO";
    if (stock > 50 && stock <= 100) return "MUY_ALTO";
    if (stock > 100) return "EXCEDENTE";
    return "DESCONOCIDO";
}
Análisis técnico:
Esta lógica puede aumentar la complejidad de la entidad y afectar la separación de responsabilidades. En un diseño más limpio, la regla de clasificación del stock podría delegarse a un servicio de dominio o a una clase especializada.
6. Cobertura de pruebas

El reporte de cobertura fue generado mediante JaCoCo y posteriormente leído por SonarQube. La cobertura inicial registrada fue de:

Cobertura inicial: X%

Este valor indica el porcentaje del código fuente que fue ejecutado por las pruebas automatizadas durante el análisis. Una cobertura baja evidencia que el proyecto requiere mayor cantidad de pruebas unitarias para validar correctamente los comportamientos del servicio y reducir el riesgo de errores no detectados.

8. Evidencia de ejecución

Durante el desarrollo del laboratorio se verificaron los siguientes puntos:

El contenedor de SonarQube fue levantado correctamente mediante Docker.
El dashboard local de SonarQube estuvo disponible en http://localhost:9000.
El proyecto Spring Boot compiló correctamente con Maven.
JaCoCo generó el archivo de reporte de cobertura en target/site/jacoco/jacoco.xml.
El análisis de SonarQube finalizó correctamente con BUILD SUCCESS.
El dashboard mostró hallazgos relacionados con bugs, code smells y cobertura.
Las capturas del dashboard fueron almacenadas en la carpeta docs/.
9. Estructura del repositorio
productos-service/
│
├── docs/
│   ├── sonar-dashboard.png
│   ├── sonar-bugs.png
│   └── sonar-code-smells.png
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── universidad/
│   │               └── productosservice/
│   │                   ├── domain/
│   │                   │   └── Producto.java
│   │                   ├── repository/
│   │                   │   └── ProductoRepository.java
│   │                   ├── service/
│   │                   │   └── ProductoService.java
│   │                   └── ProductosServiceApplication.java
│   │
│   └── test/
│
├── pom.xml
├── sonar-project.properties
└── README.md
10. Commits realizados

El repositorio contiene al menos tres commits descriptivos, correspondientes al avance progresivo del laboratorio:

1. Configuración inicial del proyecto Spring Boot
2. Implementación de código con problemas intencionales de calidad
3. Documentación del análisis inicial con SonarQube
11. Conclusión

El análisis inicial con SonarQube permitió identificar problemas relevantes de calidad en el proyecto Productos Service, principalmente relacionados con bugs potenciales, code smells, baja mantenibilidad y cobertura de pruebas insuficiente. La integración con JaCoCo permitió complementar el análisis estático con información sobre cobertura, lo cual resulta fundamental para evaluar la confiabilidad del código.

Este laboratorio evidencia la importancia de utilizar herramientas de inspección automática dentro del ciclo de desarrollo, ya que permiten detectar defectos tempranos, mejorar la mantenibilidad del software y establecer una base objetiva para futuras actividades de refactorización.