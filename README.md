# fi-spring-august-2026

Spring Boot Seminar - Skript

Dieses Dokument ist das Vortrags-Skript zum Seminar. Es wird Schritt
fuer Schritt aufgebaut - jedes Kapitel gehoert zu einem
Beispielprojekt in diesem Repository:

1. `CalculatorProject` -> Dependency Injection (DI) OHNE Spring
2. `SpringConsoleApp` -> Spring-Grundlagen (Bean, Lombok, Konfliktaufloesung, automatische Verdrahtung)
3. `WebApp` -> REST-Endpoints (Presentation Layer)

---

## Tag 1

### Kapitel 1: Das CalculatorProject - Dependency Injection verstehen

#### Ziel dieses Kapitels

Bevor wir Spring anfassen, wollen wir verstehen, welches Problem
Spring eigentlich loest. Dependency Injection (DI) ist kein
Spring-Feature, sondern ein Entwurfsprinzip. Spring ist nur ein
Werkzeug, das uns dieses Prinzip abnimmt und automatisiert.

Am `CalculatorProject`-Beispiel bauen wir DI von Hand nach - "Poor
Man's Dependency Injection". In Kapitel 2 (`SpringConsoleApp`) bauen
wir genau dieselben Calculator-Klassen dann als Spring-Beans wieder
auf - dort sehen die Teilnehmer direkt, was Spring uns an Handarbeit
abnimmt.

#### 1.1 Das Problem: Feste Kopplung

Stellen wir uns vor, eine Klasse wuerde ihre Abhaengigkeit selbst
erzeugen:

```java
public class CalcClient {
    private final CalculatorImpl calculator = new CalculatorImpl();
    ...
}
```

Frage an die Teilnehmer: Was ist hier das Problem?

- `CalcClient` ist fest an EINE konkrete Implementierung gekettet
  (`CalculatorImpl`). Ein Austausch (z.B. gegen eine Version mit
  Logging) bedeutet: Code von `CalcClient` aendern.
- `CalcClient` kann nicht mehr isoliert getestet werden (kein
  Austausch durch ein Test-Double/Mock moeglich).
- Verantwortung ist vermischt: `CalcClient` soll rechnen lassen,
  nicht entscheiden, WIE der Calculator gebaut wird.

> **Merksatz:** "Eine Klasse soll ihre Abhaengigkeiten bekommen, nicht
> sich selbst besorgen." (Hollywood-Prinzip: *"Don't call us, we'll
> call you."*)

#### 1.2 Schritt 1 - Gegen eine Abstraktion programmieren

Datei: `CalculatorProject/src/math/Calculator.java`

```java
public interface Calculator {
    double add(double a, double b);
    double sub(double a, double b);
}
```

Der erste und wichtigste Schritt zu DI ist banal, wird aber gerne
uebersehen: Wir brauchen ueberhaupt erst eine Abstraktion (ein
Interface), gegen die programmiert werden kann. Ohne Interface gibt
es nichts, was man "injizieren" koennte - nur konkrete Klassen.

Die konkrete Implementierung:

Datei: `CalculatorProject/src/math/CalculatorImpl.java`

```java
public class CalculatorImpl implements Calculator {

    @Override
    public double add(double a, double b){
        return a+b;
    }

    @Override
    public double sub(double a, double b){
        return add(a, -b);
    }
}
```

Wichtig fuer die Diskussion: `sub()` ruft `add()` auf. Das ist bewusst
so gewaehlt - spaeter, wenn wir Logging/Security als Wrapper um den
Calculator legen, sehen die Teilnehmer, dass ein interner Aufruf
(`this.add`) NICHT durch den Wrapper laeuft. Das ist ein typisches
Proxy-/AOP-Problem, auf das wir in 1.4 zurueckkommen.

#### 1.3 Schritt 2 - Der Client bekommt seine Abhaengigkeit von aussen

Datei: `CalculatorProject/src/client/CalcClient.java`

```java
public class CalcClient {

    private final Calculator calculator;

    public CalcClient(final Calculator calculator) {
        this.calculator = calculator;
    }

    public void go() {
        System.out.println(calculator.add(1, 2));
    }
}
```

Das ist Dependency Injection in Reinform - noch ganz ohne Spring:

- `CalcClient` kennt nur das Interface `Calculator`, keine konkrete
  Klasse.
- Die Abhaengigkeit wird von AUSSEN ueber den Konstruktor
  hereingereicht ("injiziert"), nicht selbst erzeugt.
- `CalcClient` entscheidet nicht, WELCHER Calculator es ist - das
  entscheidet der Aufrufer.

Das ist bereits die von Spring empfohlene Form der Injektion:
Konstruktor-Injektion.

Frage an die Teilnehmer: Wer "entscheidet" jetzt, welcher Calculator
tatsaechlich verwendet wird? -> Antwort: noch niemand. Das ist der
Job des Aufrufers/der Verdrahtung. Diese Verantwortung wandert immer
weiter nach aussen, bis sie am Ende beim Spring-Container landet.

#### 1.4 Schritt 3 - Dekorierer: Abhaengigkeiten kombinieren

Dateien: `CalculatorProject/src/math/CalculatorLogger.java`,
`CalculatorProject/src/math/CalculatorSecure.java`

```java
public class CalculatorLogger implements Calculator {

    private final Calculator calculator;

    public CalculatorLogger(Calculator calculator) {
        this.calculator = calculator;
    }

    @Override
    public double add(final double a, final double b) {
        System.out.println("Adding " + a + " " + b);
        return calculator.add(a, b);
    }

    @Override
    public double sub(final double a, final double b) {
        return calculator.sub(a, b);
    }
}
```

`CalculatorSecure` ist baugleich aufgebaut (siehe Datei) - es meldet
sich vor der eigentlichen Berechnung.

Das Entscheidende: `CalculatorLogger` IST SELBST EIN `Calculator` (es
implementiert das Interface) UND es BRAUCHT EINEN `Calculator` (im
Konstruktor). Damit lassen sich beliebig viele Calculator-Varianten
wie Matroschka-Puppen ineinanderstecken - jede Schicht fuegt ein
Verhalten hinzu (Logging, Security, ...), ohne `CalculatorImpl` oder
`CalcClient` anzufassen.

Das ist das Decorator-Pattern - und es funktioniert nur, WEIL wir in
1.2/1.3 konsequent gegen das Interface `Calculator` programmiert
haben.

> **Merksatz:** DI ist die Voraussetzung dafuer, dass sich Verhalten
> wie mit Bausteinen zusammenstecken laesst, ohne bestehenden Code zu
> aendern (Open/Closed-Prinzip).

#### 1.5 Schritt 4 - Ausblick: Der dynamische Proxy (`LoggerProxy`)

Datei: `CalculatorProject/src/common/LoggerProxy.java`

```java
public class LoggerProxy implements InvocationHandler {

    public static Object newInstance(Object obj) {
        return Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new LoggerProxy(obj));
    }

    public Object invoke(Object proxy, Method m, Object[] args)
            throws Throwable {
        Object result;
        try {
            System.out.println("before method " + m.getName());
            result = m.invoke(obj, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            System.out.println("after method " + m.getName());
        }
        return result;
    }
}
```

Statt fuer jedes Querschnittsthema (Logging, Security, ...) eine
eigene Decorator-Klasse zu schreiben, erzeugt hier die Java
Reflection-API zur Laufzeit ein Objekt, das das `Calculator`-Interface
implementiert, OHNE dass wir eine `CalculatorXyz`-Klasse dafuer
schreiben muessen. Jeder Methodenaufruf laeuft durch `invoke()` -
davor und danach koennen wir beliebigen Code einschieben.

Das ist noch kein Spring, aber es ist genau das Prinzip, mit dem
Spring AOP (Aspect Oriented Programming) unter der Haube arbeitet.

#### 1.6 Schritt 5 - main(): Die manuelle Verdrahtung

Datei: `CalculatorProject/src/main/Main.java`

```java
Calculator calc = new CalculatorImpl();

//calc = new CalculatorLogger(calc);
calc = (Calculator) LoggerProxy.newInstance(calc);
calc = new CalculatorSecure(calc);
CalcClient calcClient = new CalcClient(calc);
calcClient.go();
```

Hier laufen alle Faeden zusammen. `main()` ist der einzige Ort im
gesamten Programm, der konkrete Klassen (`new CalculatorImpl()`,
`new CalculatorSecure(...)`) kennt und der WEISS, welche Kombination
von Decorators/Proxies verwendet werden soll.

Genau diese Stelle - das manuelle `new` und "Zusammenstecken" von
Objektgraphen - ist es, was uns spaeter ein Framework abnimmt. Man
nennt dieses Prinzip Inversion of Control (IoC): Nicht mehr unser
Code entscheidet aktiv, welche Implementierung gebraucht wird (`new
CalculatorImpl()`), sondern ein Container liest eine Konfiguration
(Annotations) und reicht uns die fertigen Objekte herein. Aus "wir
holen uns etwas" (`new`) wird "wir bekommen etwas gegeben"
(injiziert) - daher der Name Dependency Injection.

> **Merksatz fuer die Teilnehmer:**
>
> - **DI** = das Prinzip: Abhaengigkeiten werden von aussen
>   hereingereicht.
> - **IoC** = die Umkehrung der Kontrolle darueber, WER diese Objekte
>   erzeugt und zusammensteckt (vom Anwendungscode zum Container).
> - `Main.java` in diesem Beispiel IST unser Container - nur eben von
>   Hand geschrieben, nicht automatisiert.

#### 1.7 Zusammenfassung und Ueberleitung zu Spring

Was die Teilnehmer aus dem `CalculatorProject`-Beispiel mitnehmen
sollen:

1. DI beginnt mit einem Interface (`Calculator`) - ohne Abstraktion
   keine Austauschbarkeit.
2. Abhaengigkeiten werden ueber den Konstruktor hereingereicht, nicht
   selbst erzeugt (`CalcClient`, `CalculatorLogger`,
   `CalculatorSecure`).
3. Weil alle Klassen gegen das Interface programmiert sind, lassen
   sich Verhalten wie Logging/Security beliebig kombinieren
   (Decorator-Pattern).
4. Ein dynamischer Proxy (`LoggerProxy`) zeigt, wie sich
   Querschnittsthemen generisch loesen lassen - der Vorlaeufer von
   Spring AOP.
5. Irgendjemand muss am Ende trotzdem entscheiden und zusammenstecken
   (`new ...`). In diesem Beispiel macht das `main()` von Hand.

Genau Punkt 5 ist die Ueberleitung: Im naechsten Kapitel
(`SpringConsoleApp`) bauen wir GENAU DIESES Beispiel (Calculator,
Logger, Secure) noch einmal - diesmal als Spring-Beans, automatisch
verdrahtet ueber den Container statt von Hand in `main()`.

---

### Kapitel 2: Das SpringConsoleApp-Projekt - Spring-Grundlagen

#### Ziel dieses Kapitels

Wir klaeren an diesem Projekt:

1. Was ist eine Bean, und wie stelle ich eine Klasse unter die
   Verwaltung von Spring?
2. Wie injiziert man mit **Lombok**, ohne Konstruktoren von Hand zu
   schreiben - und worauf man dabei achten muss?
3. Wie injiziert man nicht nur Objekte, sondern auch einfache Werte
   (`@Value`)?
4. Was ist der Unterschied zwischen Singleton und Prototype?
5. Was passiert, wenn Spring mehrere Kandidaten fuer eine
   Abhaengigkeit findet - und wie loesen wir den Konflikt auf?
6. Der grosse Moment: Wie wird aus dem handverdrahteten
   `Main.java` aus Kapitel 1 eine automatische Verdrahtung durch
   Spring?

`@Configuration`/`@Bean` (die "Fabrik-Methode" fuer Beans, die wir
nicht selbst annotieren koennen) heben wir uns fuer ein spaeteres
Kapitel auf.

#### 2.1 Vom manuellen `main()` zum Spring-Container

Datei: `SpringConsoleApp/src/main/java/de/fi/springconsoleapp/SpringConsoleAppApplication.java`

```java
@SpringBootApplication
public class SpringConsoleAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringConsoleAppApplication.class, args);
    }
}
```

Vergleichen wir das mit `CalculatorProject/src/main/Main.java` aus
Kapitel 1: Dort haben WIR in `main()` jedes Objekt selbst erzeugt und
verdrahtet. Hier tut `main()` scheinbar nichts dergleichen mehr - es
ruft nur `SpringApplication.run(...)` auf.

`SpringApplication.run()` startet den **ApplicationContext** - das
ist der Spring-Container. Ab jetzt ist der Container das neue
"`Main.java`": Er durchsucht unser Projekt, entscheidet, welche
Objekte gebraucht werden, erzeugt sie und steckt sie zusammen -
automatisch, statt wie in Kapitel 1 von Hand.

#### 2.2 Was ist eine Bean? - `@Component`

Datei: `SpringConsoleApp/src/main/java/de/fi/springconsoleapp/demo/Demo.java`

```java
@Component
@Scope("singleton") // default
//@Scope("prototype") // kein default
//@Lazy(true)
@RequiredArgsConstructor
public class Demo {

    private final Translator translator;

    @Value("${Demo.gruss}")
    private final String message;

    @PostConstruct
    public void init() {
        System.out.println(translator.translate("Post Construct von Demo"));
        System.out.println(message);
    }

    @PreDestroy
    public void peter() {
        System.out.println(translator.translate("Pre Destroy von Demo"));
    }
}
```

Eine **Bean** ist ein ganz normales Java-Objekt, das nicht von
unserem eigenen Code erzeugt und verwaltet wird, sondern vom
Spring-Container. `@Component` stellt eine Klasse unter genau diese
Verwaltung - beim Start durchsucht Spring Boot automatisch das
Package der `@SpringBootApplication`-Klasse und alle Unterpakete
(**Component-Scan**) danach.

Neben `@Component` gibt es fachliche Spezialisierungen, die technisch
dasselbe tun (die Klasse wird eine Bean), aber die Absicht der Klasse
ausdruecken: `@Service` (Business-Logik), `@Repository`
(Datenzugriff), `@Controller`/`@RestController` (Web-Anfragen) - die
lernen wir im Detail erst im `WebApp`-Kapitel kennen.

#### 2.3 Injektion mit Lombok: `@RequiredArgsConstructor`

Kein Konstruktor in `Demo` zu sehen - trotzdem wird `translator`
injiziert. Der Grund: `@RequiredArgsConstructor` (Lombok) erzeugt zur
Kompilierzeit automatisch genau den Konstruktor, den wir in Kapitel 1
bei `CalcClient` von Hand geschrieben haben - einen Parameter pro
`final`-Feld. Spring sieht am Ende exakt denselben Bytecode wie bei
einem handgeschriebenen Konstruktor und injiziert genauso ueber
diesen einzigen Konstruktor (Konstruktor-Injektion bleibt die von
Spring empfohlene Form - Lombok spart hier nur Schreibarbeit, aendert
aber nichts am Prinzip).

**Eine Falle, die man kennen sollte:** `@Value("${Demo.gruss}")` steht
in `Demo` auf dem FELD `message`, nicht auf einem Konstruktor-
Parameter. Normalerweise kopiert Lombok Feld-Annotationen NICHT
automatisch auf die generierten Konstruktor-Parameter - und genau dort
muesste `@Value` stehen, damit Spring den Wert beim Aufruf des
Konstruktors einsetzen kann. Deshalb steht im Projekt eine
`lombok.config`:

```
lombok.copyableAnnotations += org.springframework.beans.factory.annotation.Qualifier
lombok.copyableAnnotations += org.springframework.beans.factory.annotation.Value
```

Diese Zeilen sagen Lombok explizit: "Kopiere `@Qualifier` und
`@Value` vom Feld auf den generierten Konstruktor-Parameter." Ohne
diese Konfiguration wuerde `message` beim Start einfach `null` bzw.
der Startup wuerde mit einem Injektionsfehler fehlschlagen. Guter
Merksatz fuer die Teilnehmer: **Lombok + Spring braucht an dieser
Stelle eine bewusste Zusatzkonfiguration - das ist keine Selbstverstaendlichkeit.**

#### 2.4 `@Value`: Werte statt Objekte injizieren

`@Autowired` (implizit ueber den generierten Konstruktor) injiziert
Objekte (Beans, hier `Translator`). `@Value("${Demo.gruss}")`
injiziert stattdessen einen einfachen Wert aus der Konfiguration:

Datei: `SpringConsoleApp/src/main/resources/application.properties`

```properties
spring.application.name=SpringConsoleApp
spring.profiles.active=production

Demo.gruss=Hallo Universum
```

`${Demo.gruss}` wird beim Hochfahren durch den Wert der gleichnamigen
Property ersetzt (`Hallo Universum`) - `init()` gibt diesen Wert
direkt aus.

#### 2.5 Bean-Lifecycle: `@PostConstruct` und `@PreDestroy`

`init()` (mit `@PostConstruct`) laeuft automatisch, NACHDEM `Demo`
konstruiert und `translator` injiziert wurde - deshalb darf man sich
hier bereits auf `translator` verlassen. `peter()` (mit
`@PreDestroy`) laeuft beim Herunterfahren des Containers.

> **Wichtig:** `@PreDestroy` ist nur bei **Singleton**-Beans
> sinnvoll. Eine **Prototype**-Bean wird nach der Erzeugung an den
> Aufrufer "ausgehaendigt" und danach vom Container nicht weiter
> verwaltet - `@PreDestroy` wuerde dafuer nie aufgerufen.

#### 2.6 Singleton vs. Prototype - `@Scope`

```java
@Scope("singleton") // default
//@Scope("prototype") // kein default
//@Lazy(true)
```

- **`singleton`** (Standard, auch ohne `@Scope`): Der Container
  erzeugt **genau eine** Instanz, normalerweise bereits beim Start.
- **`prototype`**: Der Container erzeugt bei **jeder** Injektion/
  Anfrage eine **neue** Instanz und gibt danach die Kontrolle ab
  (siehe 2.5).

`@Lazy(true)` (auskommentiert) wuerde die Erzeugung einer
Singleton-Bean vom Containerstart auf die erste tatsaechliche
Verwendung verschieben.

**Uebung fuer die Teilnehmer:** Zwischen `@Scope("singleton")` und
`@Scope("prototype")` umschalten und beobachten, wie oft Konstruktor,
`init()` und `peter()` aufgerufen werden.

#### 2.7 Wenn mehrere Kandidaten passen: Konflikte aufloesen

Dateien: `translator/Translator.java`, `translator/ToLowerTranslator.java`,
`translator/ToUpperTranslator.java`

```java
public interface Translator {
    String translate(String input);
}
```

```java
@Component
//@Qualifier("lower")
//@Primary
@Profile("test")
public class ToLowerTranslator implements Translator {
    public String translate(final String input) { return input.toLowerCase(); }
}
```

```java
@Component
//@Qualifier("upper")
@Profile({"dev","production"})
public class ToUpperTranslator implements Translator {
    public String translate(String input) { return input.toUpperCase(); }
}
```

`Demo` fragt nach einem `Translator` - es gibt aber ZWEI
Implementierungen. Ohne weitere Hilfe sucht Spring **by Type**; gibt
es mehrere Treffer, zusaetzlich **by Name** (fragiler Fallback ueber
den Parameter-/Feldnamen). Sauberere Alternativen:

1. **`@Primary`** (hier auskommentiert bei `ToLowerTranslator`) -
   bevorzugter Kandidat im Konfliktfall.
2. **`@Qualifier("name")`** (hier auskommentiert bei beiden Klassen) -
   punktgenaue Auswahl ueber einen Namen.
3. **`@Profile("...")`** - die Bean wird ueberhaupt nur registriert,
   wenn das Profil aktiv ist.

**Was hier tatsaechlich aktiv ist:** `application.properties` setzt
`spring.profiles.active=production`. `ToUpperTranslator` traegt
`@Profile({"dev","production"})` und ist damit aktiv;
`ToLowerTranslator` traegt `@Profile("test")` und bleibt draussen -
kein Konflikt, `Demo` bekommt automatisch `ToUpperTranslator`.

**Uebung fuer die Teilnehmer:** `spring.profiles.active` auf `test`
stellen (jetzt ist `ToLowerTranslator` aktiv) - oder beide Profile
gleichzeitig aktivieren, um bewusst einen Konflikt zu provozieren und
ihn dann mit `@Primary`/`@Qualifier` aufzuloesen.

#### 2.8 Vom manuellen `Main.java` zur automatischen Verdrahtung

Jetzt der Bogen zurueck zu Kapitel 1: Genau dieselben vier Klassen -
`Calculator`, `CalculatorImpl`, `CalculatorLogger`, `CalculatorSecure`
- gibt es hier noch einmal, unter
`SpringConsoleApp/src/main/java/de/fi/springconsoleapp/math/` - aber
diesmal als Spring-Beans:

```java
@Component
@Qualifier("impl")
public class CalculatorImpl implements Calculator {
    public double add(double a, double b){ return a+b; }
    public double sub(double a, double b){ return add(a, -b); }
}
```

```java
@Component
@Qualifier("logger")
public class CalculatorLogger implements Calculator {

    private final Calculator calculator;

    public CalculatorLogger(@Qualifier("impl") Calculator calculator) {
        this.calculator = calculator;
    }
    ...
}
```

```java
@Component
@Qualifier("secure")
public class CalculatorSecure implements Calculator {

    private final Calculator calculator;

    public CalculatorSecure(@Qualifier("logger") final Calculator calculator) {
        this.calculator = calculator;
    }
    ...
}
```

Vergleicht das mit `Main.java` aus Kapitel 1:

```java
Calculator calc = new CalculatorImpl();
calc = (Calculator) LoggerProxy.newInstance(calc);
calc = new CalculatorSecure(calc);
```

Dort haben WIR in genau dieser Reihenfolge von Hand verschachtelt.
Hier passiert dasselbe, nur deklarativ: Jede Implementierung bekommt
per `@Qualifier` einen eigenen Namen (`"impl"`, `"logger"`,
`"secure"`), und jeder Konstruktor sagt per `@Qualifier` an seinem
Parameter genau, WELCHEN der drei `Calculator`-Kandidaten er will.
Der Container baut daraus beim Start automatisch dieselbe Kette:
`CalculatorSecure` -> (injiziert) `CalculatorLogger` -> (injiziert)
`CalculatorImpl`.

Wer diese Bean wieder herausbekommen will, muss nur noch danach
fragen - hier vorbereitet (aktuell noch deaktiviert, siehe unten) in
`runner/MyRunner.java`:

```java
//@Component
@RequiredArgsConstructor
public class MyRunner implements CommandLineRunner {

    @Qualifier("secure")
    private final Calculator calculator;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Hello World");
        System.out.println(calculator.add(1, 2));
    }
}
```

> **Das ist der zentrale Moment dieses Kapitels:** `Main.java` (Kapitel
> 1) UND `MyRunner` (hier) fuehren am Ende zur selben verschachtelten
> Objektstruktur (`Secure` um `Logger` um `Impl`). Der einzige
> Unterschied ist, WER die Verdrahtung uebernimmt - wir von Hand
> (`new`, `Main.java`) oder der Container automatisch (`@Component`
> + `@Qualifier`).

#### 2.9 Ausblick: Noch vorbereitet, aber noch nicht aktiv

Zwei weitere Bausteine liegen bereits im Projekt, sind aber aktuell
bewusst deaktiviert (`//@Component`) bzw. noch nicht verdrahtet -
Stoff fuer eine der naechsten Einheiten:

- **`runner/OtherRunner.java`** zeigt mit `@Order(1)`, wie sich die
  Ausfuehrungsreihenfolge mehrerer `CommandLineRunner`-Beans steuern
  laesst, sobald mehr als einer aktiv ist.
- **`demo/Person.java`** ist ein reines Lombok-Beispiel (`@Data`,
  `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`) - noch ohne
  Bezug zu `Demo`. Genau dieses Lombok-Muster (`@Data`/`@Builder`)
  taucht in Kapitel 3 bei `PersonDto` wieder auf.

#### 2.10 Zusammenfassung

1. **Bean** = ein von Spring verwaltetes Objekt; `@Component` stellt
   eine Klasse unter diese Verwaltung, der Component-Scan findet sie
   automatisch.
2. **Lombok** (`@RequiredArgsConstructor`) erzeugt denselben
   Konstruktor, den wir in Kapitel 1 von Hand geschrieben haben -
   Konstruktor-Injektion bleibt das Prinzip, nur der Code wird
   kuerzer. Achtung bei `@Value`/`@Qualifier` auf Feldern:
   `lombok.config` muss diese Annotationen explizit auf den
   generierten Konstruktor kopieren.
3. **`@Value`** injiziert Werte (Strings, Zahlen, ...) aus
   `application.properties`, nicht Objekte.
4. **`Singleton`** (Standard) vs. **`Prototype`** (`@Scope`) und
   **`@PostConstruct`**/**`@PreDestroy`** fuer den Bean-Lifecycle.
5. Konflikte bei mehreren Kandidaten: `@Primary`, `@Qualifier`,
   `@Profile`.
6. Der wichtigste Punkt: Die manuelle Verdrahtung aus `Main.java`
   (Kapitel 1) und die automatische Verdrahtung ueber `@Component`/
   `@Qualifier` (hier) fuehren zum selben Ergebnis - Spring
   automatisiert exakt das, was wir in Kapitel 1 von Hand gemacht
   haben.

---

### Kapitel 3: WebApp - Von der Konsole ins Web (Anfang)

#### Ziel dieses Kapitels

`WebApp` bleibt (anders als `SpringConsoleApp`) dauerhaft laufen und
wartet auf HTTP-Anfragen - dafuer bringt Spring Boot ueber
`spring-boot-starter-webmvc` einen eingebetteten Server mit. Wir
beginnen bewusst nur mit dem **Presentation Layer** (REST-Endpoints);
Persistence-, Service- und Domain Layer sind in diesem Projektstand
noch leer (siehe die `.gitkeep`-Dateien unter `persistence/` und
`service/`) und folgen an einem der naechsten Tage.

#### 3.1 REST-Grundprinzipien: Ressourcen-Naming

Grundlage: [restfulapi.net - Resource Naming](https://restfulapi.net/resource-naming/)

- Eine URL bezeichnet eine **Ressource** (ein Nomen), keine Aktion:
  `/personen`, nicht `/getPersonen`.
- Ressourcen-Sammlungen werden im **Plural** benannt: `/personen`
  liefert viele, `/personen/{id}` genau eine.
- Die **Aktion** wird ueber das **HTTP-Verb** transportiert (`GET`,
  `POST`, `PUT`, `DELETE`), nicht in die URL geschrieben.
- Eine sinnvolle **Versionierung** (hier `/v1/...`) macht die
  Schnittstelle stabil erweiterbar.

#### 3.2 Das Prinzip an einem Minimalbeispiel: `DemoController`

Datei: `WebApp/src/main/java/de/fi/webapp/presentation/controller/DemoController.java`

```java
@RestController
@RequestMapping("/demo")
public class DemoController {

    @GetMapping(path = "/gruss", produces = MediaType.TEXT_PLAIN_VALUE)
    public String gruss() {
        return "Hallo Rest";
    }
}
```

- `@RestController` ist eine spezialisierte `@Component`-Bean
  (Kapitel 2) und sorgt zusaetzlich dafuer, dass Rueckgabewerte
  direkt in den HTTP-Response-Body geschrieben werden.
- `@RequestMapping("/demo")` legt den gemeinsamen Basis-Pfad fest,
  `@GetMapping("/gruss")` bildet `GET` auf `gruss()` ab -> zusammen
  `GET /demo/gruss`.

Mehr braucht es nicht, um das Kernprinzip zu zeigen: **Eine Methode +
Annotationen = ein HTTP-Endpoint.**

#### 3.3 Ein vollstaendiger REST-Endpoint: `PersonenController` (noch ohne Service)

Datei: `WebApp/src/main/java/de/fi/webapp/presentation/controller/PersonenController.java`

```java
@RestController
@RequestMapping("/v1/personen")
public class PersonenController {

    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<PersonDto> getPerson(@PathVariable UUID id) {
        if (id.toString().endsWith("1")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(PersonDto.builder().id(id).vorname("Max").nachname("Mustermann").build());
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Iterable<PersonDto>> getPersonen(
            @RequestParam(required = false, defaultValue = "Fritz") String vorname,
            @RequestParam(required = false, defaultValue = "Schmitt") String nachname
    ) {
        System.out.printf("Vorname = %s, Nachname = %s\n", vorname, nachname);
        var list = List.of(
            PersonDto.builder().id(UUID.randomUUID()).vorname("John").nachname("Doe").build(),
            PersonDto.builder().id(UUID.randomUUID()).vorname("John").nachname("Wick").build()
            // ...
        );
        return ResponseEntity.ok(list);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> loeschePerson(@PathVariable UUID id) {
        if (id.toString().endsWith("1")) {
            return ResponseEntity.notFound().build();
        }
        System.out.println("Person mit der ID: " + id + " wurde gelöscht!");
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> einfuegen(@Valid @RequestBody PersonDto personDto, UriComponentsBuilder uriBuilder) {
        UriComponents uriComponents = uriBuilder.path("/v1/personen/{id}").buildAndExpand(personDto.getId());
        return ResponseEntity.created(uriComponents.toUri()).build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody PersonDto personDto) {
        if (id.equals(personDto.getId())) throw new IdMismatchException("Upps");
        return ResponseEntity.ok().build();
    }
}
```

Bewusster Unterschied zu einem "fertigen" Controller: Dieser hier hat
**noch gar keine Abhaengigkeit** zu einem Service - kein Konstruktor,
kein injiziertes Interface. Er liefert Daten zurueck, die er sich
selbst ausdenkt (`PersonDto.builder()...build()`, Lombok - siehe
2.9), und simuliert "nicht gefunden", indem er einfach prueft, ob die
`id` mit `"1"` endet. Das ist didaktisch Absicht: So laesst sich die
REST-Mechanik (Pfade, Verben, Statuscodes, `@Valid`,
`ResponseEntity`) isoliert besprechen, BEVOR Service und Persistence
dazukommen.

Trotzdem gelten dieselben Bausteine wie ueberall:

- `@PathVariable` (Ressourcen-ID) vs. `@RequestParam` (Filter auf
  einer Sammlung, hier mit `defaultValue`).
- `@Valid @RequestBody PersonDto` aktiviert die Bean-Validation aus
  `PersonDto` (`@NotNull`, `@Size`) - ungueltige Anfragen werden
  automatisch mit `400 Bad Request` beantwortet, bevor die
  Methode ueberhaupt betreten wird.
- `POST` liefert bei Erfolg `201 Created` mit `Location`-Header
  (`UriComponentsBuilder`), statt nur `200 OK`.
- `IdMismatchException` (`presentation/exception/IdMismatchException.java`)
  ist eine ungeprueft Exception fuer den Fall widerspruechlicher IDs
  zwischen Pfad und Body.

> **Zum Selbst-Pruefen:** Die Bedingung in `update()`
> (`if (id.equals(personDto.getId())) throw ...`) wirft die Exception
> aktuell, WENN die IDs uebereinstimmen - fachlich wuerde man eher
> erwarten, dass genau der gegenteilige Fall (IDs weichen voneinander
> ab) den Fehler ausloest. Lohnt sich, im Kurs bewusst gemeinsam mit
> den Teilnehmern zu entdecken (oder vorher selbst zu entscheiden, ob
> das so gewollt ist).

Datei: `WebApp/src/main/java/de/fi/webapp/presentation/dto/PersonDto.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonDto {

    @NotNull
    private UUID id;

    @NotNull
    @Size(min = 2, max = 50)
    private String vorname;

    @NotNull
    @Size(min = 2, max = 50)
    private String nachname;
}
```

Wieder Lombok (siehe 2.9): `@Data` erzeugt Getter/Setter/
`equals`/`hashCode`/`toString`, `@Builder` ermoeglicht die
`PersonDto.builder()...build()`-Schreibweise, die der Controller oben
fuer seine Test-/Fake-Daten nutzt.

### Noch offen

- **Persistence-, Service- und Domain Layer** fuer `WebApp` -
  aktuell nur leere Packages (`persistence/`, `service/`), folgen an
  einem der naechsten Tage.
- `OtherRunner` (`@Order`) und `Person.java` (Lombok) aus Kapitel 2 -
  vorbereitet, noch nicht im Detail behandelt.
- `@Configuration`/`@Bean` - bewusst zurueckgestellt.
