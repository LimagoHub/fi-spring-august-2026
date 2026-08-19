# fi-spring-august-2026

Spring Boot Seminar - Skript

Dieses Dokument ist das Vortrags-Skript zum Seminar. Es ist auf drei
Seminartage aufgeteilt und wird Schritt fuer Schritt aufgebaut -
jedes Kapitel gehoert zu einem Beispielprojekt in diesem Repository:

**Tag 1**
1. `CalculatorProject` -> Dependency Injection (DI) OHNE Spring
2. `SpringConsoleApp` -> Spring-Grundlagen (Bean, Lombok, Konfliktaufloesung, automatische Verdrahtung)
3. `WebApp` -> REST-Endpoints (Presentation Layer)

**Tag 2**
4. `WebApp` (Fortsetzung) -> Persistence Layer mit Spring Data, Domain Layer

**Tag 3**
5. `WebApp` (Fortsetzung) -> Service-/Business-Layer, `@Configuration`/`@Bean`, Fehlerbehandlung, groessere Uebung (`Schwein`)

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

- `OtherRunner` (`@Order`) und `Person.java` (Lombok) aus Kapitel 2 -
  vorbereitet, noch nicht im Detail behandelt.

---

## Tag 2

### Kapitel 3 (Fortsetzung): Persistence Layer (Spring Data JPA) und Domain Layer

Wir bleiben in `WebApp` und gehen jetzt eine Schicht tiefer: Woher
kommen eigentlich die Daten, die `PersonenController` (Tag 1) ueber
`service.findeAlle()` bzw. `service.findeNachId(id)` liefert? Die
Antwort liegt im Package `persistence` - und dort setzt Spring Data
JPA genau das DI-Prinzip aus Kapitel 1 fort: Wir schreiben ein
Interface, Spring liefert uns zur Laufzeit die passende
Implementierung als Bean.

Bewusst zurueckgestellt fuer heute: Wie `PersonServiceImpl` den
`PersonenRepository` konkret benutzt (Business-/Service-Layer, Tag
3) - wir schauen uns die Persistence-Schicht zunaechst fuer sich an.

#### 3.4 Vom Java-Objekt zur Datenbanktabelle: `@Entity`

Datei: `WebApp/src/main/java/de/fi/webapp/persistence/entity/PersonEntity.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "tbl_personen")
public class PersonEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String vorname;

    @Column(nullable = false, length = 50)
    private String nachname;
}
```

- `@Entity` stellt die Klasse unter die Verwaltung von JPA/Hibernate -
  eine Instanz entspricht ab jetzt einer Zeile in einer
  Datenbanktabelle.
- `@Table(name = "tbl_personen")` legt fest, in welcher Tabelle diese
  Zeilen landen (ohne diese Annotation wuerde der Klassenname
  verwendet) - `src/main/resources/data.sql` befuellt genau diese
  Tabelle beim Start bereits mit 100 Testdatensaetzen.
- `@Id` markiert das Feld, das den Primärschlüssel abbildet - hier
  eine `UUID`, die wir selbst vergeben (kein `@GeneratedValue`).
- `@Column` steuert Details der Spalte (`nullable`, `length`, ...).

Auffaellig gegenueber Kapitel 2: `PersonEntity` kombiniert Lombok
(`@Data`/`@NoArgsConstructor`/`@AllArgsConstructor`/`@Builder`) UND
JPA-Annotationen auf derselben Klasse. Das ist kein Widerspruch -
Lombok generiert nur Boilerplate (Getter/Setter/Konstruktoren),
JPA/Hibernate braucht davon vor allem den parameterlosen Konstruktor
(`@NoArgsConstructor`), um beim Auslesen aus der Datenbank per
Reflection ein leeres Objekt zu erzeugen und anschliessend zu
befuellen.

`PersonEntity` sieht `PersonDto` (Tag 1, Presentation Layer) sehr
aehnlich - beide haben `id`, `vorname`, `nachname`. Trotzdem sind es
bewusst zwei getrennte Klassen: Die eine beschreibt, WIE Daten ueber
HTTP transportiert werden (inkl. Bean-Validation-Regeln), die andere,
WIE Daten in der Datenbank liegen (inkl. Spaltenlaenge, Nullability).
Beide zu vermischen wuerde die Schichten unnoetig aneinanderketten.

#### 3.5 Repository: Interface statt Implementierung

Datei: `WebApp/src/main/java/de/fi/webapp/persistence/repository/PersonenRepository.java`

```java
public interface PersonenRepository extends CrudRepository<PersonEntity, UUID> {

    Iterable<PersonEntity> findByVorname(String vorname);

    @Query("select new de.fi.webapp.persistence.entity.TinyPerson(p.id, p.nachname) from PersonEntity p")
    Iterable<TinyPerson> egal();

    Iterable<TinyPerson> findAllProjectByVorname(String vorname);
}
```

Das ist der eigentliche "Aha-Moment" von Spring Data:
`PersonenRepository` ist nur ein **Interface** - es gibt nirgendwo im
Projekt eine Klasse, die `implements PersonenRepository` schreibt und
`save()`, `findById()` oder `findAll()` von Hand implementiert.
Trotzdem laesst sich `PersonenRepository` ganz normal per Konstruktor
injizieren (siehe `PersonServiceImpl`, Tag 3) und funktioniert.

Der Grund: Spring Data erzeugt zur Laufzeit selbst eine
Implementierung dieses Interfaces und registriert sie als Bean -
technisch ueber genau das Prinzip, das wir in Kapitel 1 an
`LoggerProxy` von Hand nachgebaut haben (ein dynamischer Proxy, der
Methodenaufrufe abfaengt und behandelt). Nur muessen wir diesmal
selbst keine Zeile Proxy-Code schreiben - `CrudRepository` bringt
bereits alle Standard-Operationen mit: `save(entity)`,
`findById(id)` -> `Optional<T>`, `findAll()`, `existsById(id)`,
`deleteById(id)`.

#### 3.6 Abgeleitete Query-Methoden - Namenskonvention statt SQL

```java
Iterable<PersonEntity> findByVorname(String vorname);
```

Diese Methode steht nur als Signatur im Interface - keine
Implementierung, kein `@Query`. Spring Data liest den
**Methodennamen** und leitet daraus die Abfrage ab: `findBy` +
`Vorname` (der Feldname aus `PersonEntity`) wird zu `SELECT ... FROM
tbl_personen WHERE vorname = ?`.

Das ist die einzige Form von "eigenen Abfragen", die wir heute im
Detail anfassen: **Namenskonvention statt SQL/JPQL.** Die beiden
anderen Methoden im selben Interface stehen zwar schon da, zeigen
aber bereits komplexeres Terrain:

> **Heute nur kurz angerissen, Details folgen in 3.19:** `egal()`
> nutzt eigenes JPQL (`@Query(...)`), das direkt ein
> `TinyPerson`-Projektions-Objekt erzeugt (ein `record` mit nur
> `id`/`nachname`, siehe `persistence/entity/TinyPerson.java`) -
> nuetzlich, wenn man nicht alle Spalten einer Entity braucht.
> `findAllProjectByVorname` zeigt, dass sich diese Projektion sogar
> mit der Methodennamen-Konvention kombinieren laesst.

#### 3.7 Zusammenfassung und weiterfuehrende Referenz

1. `@Entity` bildet eine Java-Klasse auf eine Datenbanktabelle ab.
2. Ein Spring-Data-Repository ist nur ein **Interface** - die
   Implementierung erzeugt der Container zur Laufzeit automatisch
   (Proxy-Prinzip aus Kapitel 1).
3. `CrudRepository` liefert die Standard-Operationen (Speichern,
   Lesen, Loeschen) ohne eine Zeile eigenen Codes.
4. Einfache eigene Abfragen lassen sich ueber die **Methodennamen-
   Konvention** (`findBy...`) erzeugen, ganz ohne SQL/JPQL.
5. Alles darueber hinaus (eigene `@Query`s, Projektionen) ist bewusst
   ausgeklammert.

Wer sich vertiefen moechte, findet die vollstaendige Dokumentation in
der offiziellen Spring-Data-JPA-Referenz:
[docs.spring.io/spring-data/jpa/reference](https://docs.spring.io/spring-data/jpa/reference/)

Wir haben jetzt zwei der drei "Personen"-Klassen im Projekt gesehen:
`PersonDto` (Presentation, Tag 1) und `PersonEntity` (Persistence,
gerade eben). Die dritte ist `Person` im Package `service.model` -
das **Domain-Modell**, mit dem wir Tag 2 abschliessen.

#### 3.8 Domain Layer: `Person` als fachliches Modell

Datei: `WebApp/src/main/java/de/fi/webapp/service/model/Person.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    private UUID id;
    private String vorname;
    private String nachname;
}
```

Auffaellig: `Person` traegt **keine** JPA-Annotationen (kein
`@Entity`) und **keine** Bean-Validation-Annotationen (kein
`@NotNull`/`@Size`). Es ist ein reines Java-Objekt ohne jede
Abhaengigkeit zu einem Framework - nur Lombok bleibt, um Getter/
Setter/Builder nicht von Hand schreiben zu muessen (Kapitel 2).

Warum drei fast identisch aussehende Klassen (`PersonDto`,
`PersonEntity`, `Person`) fuer dasselbe fachliche Ding? Jede Schicht
hat ihre eigene, nur fuer sie relevante Sicht:

- `PersonDto` (Presentation) - wie sieht die Person "auf der
  Leitung" (HTTP/JSON) aus, inkl. Regeln, die nur beim Entgegennehmen
  von Aussen zaehlen (Bean Validation).
- `PersonEntity` (Persistence) - wie liegt die Person in der
  Datenbank (Tabellen-/Spaltenabbildung).
- `Person` (Domain) - was IST eine Person fachlich, unabhaengig
  davon, ob sie gerade per HTTP hereinkommt oder aus der Datenbank
  gelesen wird.

Der Service-Layer (Tag 3) arbeitet ausschliesslich mit `Person` - er
kennt weder `PersonDto` noch `PersonEntity`. Die Umwandlung zwischen
den Schichten uebernehmen eigene Mapper (`PersonDtoMapper` an der
Controller-Grenze, `PersonMapper` an der Repository-Grenze).

#### 3.9 Exkurs: Die Mapper - MapStruct statt Hand-Code

Dateien: `WebApp/src/main/java/de/fi/webapp/service/mapper/PersonMapper.java`,
`WebApp/src/main/java/de/fi/webapp/presentation/mapper/PersonDtoMapper.java`

```java
@Mapper(componentModel = "spring")
public interface PersonMapper {
    Person convert(PersonEntity personEntity);
    PersonEntity convert(Person person);
    Iterable<Person> convert(Iterable<PersonEntity> personEntity);
}
```

Auch hier wieder nur ein **Interface**, keine Implementierung im
Projekt - genau wie bei `PersonenRepository` (3.5). Trotzdem laesst
sich `PersonMapper` per Konstruktor injizieren. Der Unterschied zu
Spring Data: Hier entsteht die Implementierung NICHT zur Laufzeit per
Proxy, sondern beim **Kompilieren** - `mapstruct-processor` (siehe
`pom.xml`, Abschnitt `annotationProcessorPaths`) generiert eine
echte Klasse `PersonMapperImpl` mit stinknormalem, lesbarem Java-Code
(Feld fuer Feld kopiert), die als `@Component` registriert wird. Wer
neugierig ist, findet diese generierte Klasse nach dem Build unter
`target/generated-sources/annotations/`.

> **Merksatz:** Zwei Frameworks, zwei Automatisierungs-Strategien,
> dasselbe Ziel (kein Boilerplate von Hand schreiben): Spring Data
> (3.5) erzeugt die Implementierung **zur Laufzeit** per dynamischem
> Proxy (wie `LoggerProxy` in Kapitel 1). MapStruct erzeugt sie
> **zur Kompilierzeit** per Annotation-Processor - kein Proxy, kein
> Reflection-Overhead zur Laufzeit, dafuer generierter Code, der bei
> jedem Build neu entsteht.

---

## Tag 3

### Kapitel 3 (Fortsetzung): Der Service-/Business-Layer

Den Domain Layer (`Person`) kennen wir bereits aus Tag 2 (siehe 3.8)
- er ist die Grundlage der Schicht, die heute im Detail dran ist: der
**Service-/Business-Layer**.

#### 3.10 Der Service: Vertrag und fachliche Pruefung

Datei: `WebApp/src/main/java/de/fi/webapp/service/PersonenService.java`

```java
public interface PersonenService {
    void speichern(Person person) throws PersonenServiceException;
    void aendern(Person person) throws PersonenServiceException;
    void loeschen(UUID uuid) throws PersonenServiceException;
    Optional<Person> findeNachId(UUID uuid) throws PersonenServiceException;
    Iterable<Person> findeAlle() throws PersonenServiceException;
}
```

Genau wie bei `Calculator` (Kapitel 1) oder `PersonenRepository` (Tag
2) gilt: Der Controller kennt nur dieses Interface, nicht
`PersonServiceImpl`. Der Service hat zwei Aufgaben, die weder
Presentation noch Persistence uebernehmen sollen: **Validierung** und
eine kleine **fachliche Pruefung**.

Datei: `WebApp/src/main/java/de/fi/webapp/service/internal/PersonServiceImpl.java`,
Methode `validieren`:

```java
private void validieren(final Person person) throws PersonenServiceException {
    if (person == null) throw new PersonenServiceException("Person darf nicht null sein");
    if (person.getVorname() == null || person.getVorname().length() < 2) throw new PersonenServiceException("Vorname zu kurz");
    if (person.getNachname() == null || person.getNachname().length() < 2) throw new PersonenServiceException("Nachname zu kurz");
    //if (blacklistService.isBlacklisted(person)) throw new PersonenServiceException("Antipath");
    if(antipathen.contains(person.getVorname()) ) throw new PersonenServiceException("Antipath");
}
```

Das sind technische Mindestanforderungen (nicht null, Mindestlaenge)
- interessanter ist die letzte Zeile, die **fachliche** Regel: Steht
der Vorname auf einer Liste unerwuenschter Personen ("Antipathen")?
Anders als man es vielleicht erwarten wuerde, verwendet dieses
Projekt bewusst **dieselbe** Exception (`PersonenServiceException`)
fuer beide Faelle - fachlich waere eine eigene, sprechendere
Exception denkbar, das Prinzip "fachliche Regeln gehoeren in den
Service, nicht in Controller oder Persistence" gilt aber so oder so.

#### 3.11 Die Transaktionsklammer: `@Transactional`

```java
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = PersonenServiceException.class, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class PersonServiceImpl implements PersonenService {
    ...
}
```

`@Transactional` auf Klassenebene spannt um **jede** Methode dieses
Service eine Transaktionsklammer - Spring startet die Transaktion
beim Methodenaufruf und committet (oder rollt zurueck) beim Verlassen
der Methode. Drei Parameter im Detail:

- `propagation = Propagation.REQUIRED` (Standardwert, hier zur
  Klarheit ausgeschrieben): Existiert bereits eine laufende
  Transaktion, wird sie **wiederverwendet**; sonst wird eine neue
  eroeffnet.
- `isolation = Isolation.READ_COMMITTED`: legt fest, wie stark diese
  Transaktion von gleichzeitig laufenden anderen Transaktionen
  abgeschirmt ist (hier: nur bereits committete Aenderungen anderer
  Transaktionen sind sichtbar).
- `rollbackFor = PersonenServiceException.class`: **das ist der
  eigentliche Lehrpunkt.** Spring rollt eine Transaktion
  standardmaessig nur bei **ungeprueften** Exceptions
  (`RuntimeException` und Unterklassen) automatisch zurueck - bei
  **geprueften** (checked) Exceptions passiert das NICHT, ausser man
  sagt es Spring explizit ueber `rollbackFor`.

Genau deshalb ist `PersonenServiceException` bewusst eine **geprueft**
Exception (`extends Exception`, nicht `RuntimeException`, siehe
`service/exception/PersonenServiceException.java`) - nicht aus
Zufall, sondern um im Kurs an einem konkreten Beispiel zu zeigen,
dass geprueft Exceptions bei `@Transactional` explizit angegeben
werden muessen, sonst wuerde z.B. ein fehlgeschlagenes
`repo.save(...)` innerhalb von `speichern()` NICHT zu einem Rollback
fuehren, obwohl die Methode selbst eine `PersonenServiceException`
wirft.

`@RequiredArgsConstructor` (Lombok, Kapitel 2) erzeugt den
Konstruktor fuer die drei `final`-Felder `repo`, `mapper` und
`antipathen` - Konstruktor-Injektion bleibt also auch hier das
Prinzip, nur handschriftlich sparen wir uns den Code.

#### 3.12 Ein Seitenblick auf SOLID: `BlacklistService`

Konsequent zu Ende gedacht (SOLID, insbesondere Single Responsibility)
gehoert eine Blacklist-Pruefung eigentlich gar nicht in
`PersonServiceImpl`, sondern in einen eigenen, dafuer zustaendigen
Service - im Projekt bereits als Interface angelegt:

Datei: `WebApp/src/main/java/de/fi/webapp/service/BlacklistService.java`

```java
public interface BlacklistService {
    boolean isBlacklisted(Person possibleBlacklistedPerson);
}
```

"Richtig" waere an dieser Stelle Konstruktor-Injektion des
`BlacklistService` - im Code bereits vorbereitet, aber auskommentiert:

```java
private final PersonenRepository repo;
private final PersonMapper mapper;
//private final BlacklistService blacklistService;

@Qualifier("antipathen")
private final List<String> antipathen;
```

Wir nutzen an dieser Stelle bewusst trotzdem (noch) keinen eigenen
`BlacklistService`, sondern bleiben bei der einfachen `List<String>`
- nicht aus fachlicher Ueberzeugung, sondern didaktisch: Eine
`List<String>`, die von irgendwoher kommen muss, ist der ideale
Anlass, um `@Configuration`/`@Bean` einzufuehren.

#### 3.13 `@Configuration` und `@Bean`

Datei: `WebApp/src/main/java/de/fi/webapp/service/config/PersonConfig.java`

```java
@Configuration
public class PersonConfig {

    @Bean
    @Qualifier("antipathen")
    public List<String> createAntipathen() {
        return List.of("Attila", "Peter","Paul", "Mary");
    }

    @Bean
    @Qualifier("fruits")
    public List<String> createFruits() {
        return List.of("Banana", "Cherry","Strawberry", "Raspberry");
    }

    /*@Bean
    public PersonenService createPersonenService(final PersonenRepository repo, final PersonMapper mapper, @Qualifier("antipathen") final List<String> antipathen) {
        return new PersonServiceImpl(repo,mapper, antipathen );
    }*/
}
```

`@Configuration` markiert eine Klasse als reine "technische" Quelle
fuer Bean-Definitionen (keine eigene fachliche Aufgabe). Jede mit
`@Bean` annotierte Methode darin ist eine kleine **Fabrik**: Der
Rueckgabewert wird als Bean im Container registriert. `@Qualifier
("antipathen")` vergibt einen Namen, damit `PersonServiceImpl` (3.12)
per `@Qualifier("antipathen")` am Konstruktor-Parameter genau DIESE
`List<String>`-Bean bekommt und nicht irgendeine andere.

Warum reicht hier kein einfaches `@Component` wie bisher? **Man kann
aus einer fremden Klasse keine eigene Komponente machen.**
`List`/`List.of(...)` sind Klassen/Methoden der Java-Standardbibliothek
- wir koennen dort kein `@Component` hinschreiben. `@Bean` ist der
einzige Weg, so ein Objekt trotzdem unter Spring-Verwaltung zu
stellen.

`createFruits()` zeigt denselben Mechanismus ein zweites Mal, diesmal
ohne fachlichen Bezug zu `Person` - rein, um zu demonstrieren, dass es
in einer `@Configuration`-Klasse mehr als eine `@Bean`-Methode
desselben Rueckgabetyps geben kann und `@Qualifier` genau dafuer
noetig ist (Konfliktaufloesung, siehe Kapitel 2, 2.7): Ohne
`@Qualifier` wuesste Spring bei der Injektion einer `List<String>`
nicht, ob `antipathen` oder `fruits` gemeint ist.

Randnotiz im Code: In `PersonConfig` findet sich auskommentiert sogar
eine `@Bean`-Methode, die `PersonServiceImpl` selbst als Bean erzeugen
wuerde - als Beleg, dass `@Bean` grundsaetzlich eine Alternative zu
`@Service`/`@Component` waere. Wir bleiben aber bei `@Service` fuer
den Service selbst.

> **Noch offen:** Ein zweites Beispiel, das `@Bean` fuer eine
> **komplexe** Erzeugung aus externen Werten zeigt (z.B. eine
> YAML-Datei per `@PropertySource`/`@ConfigurationProperties`), gibt
> es in diesem Projekt bisher nicht - moeglicher Kandidat fuer einen
> der naechsten Termine.

#### 3.14 Unit-Testing mit Mockito (Ausblick)

Datei: `WebApp/src/test/java/de/fi/webapp/service/internal/PersonServiceImplTest.java`

```java
@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {
    @InjectMocks
    private PersonServiceImpl objectUnderTest;

    @Mock
    private PersonenRepository personRepositoryMock;

    @Mock
    private PersonMapper mapperMock;

    @Mock
    private List<String> antipathenMock;

    @Test
    void speichern__person_is_null__PersoneServiceExceotionIsThrown() {
        final PersonenServiceException ex = assertThrows(PersonenServiceException.class, ()->objectUnderTest.speichern(null));
        assertEquals("Person darf nicht null sein", ex.getMessage());
    }
}
```

Dieser Test startet **keinen** Spring-Container - kein
`@SpringBootTest`, kein `ApplicationContext`. `@Mock` erzeugt fuer
`PersonenRepository`, `PersonMapper` und die `antipathen`-Liste (3.13)
je ein Test-Double, `@InjectMocks` reicht diese Mocks per Konstruktor
in `objectUnderTest` hinein - das funktioniert nur, **weil**
`PersonServiceImpl` von Anfang an konsequent auf Konstruktor-Injektion
gegen Interfaces gesetzt hat (Kapitel 1).

Der erste Test prueft genau den ersten `if`-Zweig aus `validieren`
(3.10): `speichern(null)` muss eine `PersonenServiceException` mit der
Nachricht "Person darf nicht null sein" werfen. Auffaellig im
Methodennamen: Statt `@DisplayName` (eine Alternative, um einen Test
lesbar zu benennen) steht hier die ganze Beschreibung direkt im
Methodennamen selbst, per Konvention mit doppeltem Unterstrich
getrennt (`<Methode>__<Szenario>__<Erwartung>`) - beide Wege fuehren
zum selben Ziel: ein Testbericht, der auch ohne Blick in den Code
verstaendlich ist.

**Weitere Uebung fuer die Teilnehmer:** Analoge Tests fuer die
uebrigen `if`-Zweige aus `validieren` ergaenzen (zu kurzer Vorname,
zu kurzer Nachname, Vorname auf der Antipathen-Liste), sowie einen
Happy-Path-Test, der `personRepositoryMock`/`mapperMock` gezielt per
`when(...)` programmiert.

### Kapitel 3 (Fortsetzung): Fehlerbehandlung

Jetzt sind alle Schichten bekannt (Presentation, Persistence, Domain,
Service) - genau die Voraussetzung, um ueber Fehlerbehandlung zu
sprechen: Jede Exception, die uns jetzt begegnet, koennen wir einer
Schicht zuordnen, in der sie entsteht.

#### 3.15 Der zentrale ErrorHandler (`@ControllerAdvice`)

Datei: `WebApp/src/main/java/de/fi/webapp/presentation/errorhandler/ErrorHandler.java`

```java
@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatusCode status, final WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getField() + ":" + x.getDefaultMessage())
                .collect(Collectors.toList());
        body.put("errors", errors);

        // WICHTIG !!!!!!
        logger.error("Upps", ex);
        return ResponseEntity.badRequest().body(body);
    }
    ...
}
```

`@ControllerAdvice` ist selbst wieder nur eine Spezialisierung von
`@Component` (Kapitel 2) - mit einer Besonderheit: Sie gilt nicht nur
fuer einen Controller, sondern **querschnittlich fuer alle**
`@RestController`-Beans der Anwendung. Genau wie `LoggerProxy`
(Kapitel 1) ist das ein Werkzeug fuer Querschnittsthemen - nur
diesmal speziell fuer Exceptions.

`ResponseEntityExceptionHandler` ist eine von Spring mitgelieferte
Basisklasse mit bereits fertigen Handlern fuer haeufige Spring-MVC-
Faelle. `handleMethodArgumentNotValid` ueberschreiben wir gezielt: Sie
feuert automatisch immer dann, wenn ein mit `@Valid` annotiertes
`@RequestBody` die Bean-Validation nicht besteht (Tag 1, 3.3,
`PersonDto`/`SchweinDto`) - der Controller-Code selbst sieht davon
nichts.

#### 3.16 Fachliche Exceptions auf Statuscodes abbilden

```java
@ExceptionHandler(PersonenServiceException.class)
public ResponseEntity<Object> handlePersonenServiceException(PersonenServiceException ex, WebRequest request) {
    ...
    body.put("type", ex.getClass().getSimpleName());// Achtung security
    logger.error("Upps", ex);
    return ResponseEntity.internalServerError().body(body);
}

@ExceptionHandler(SchweineServiceException.class)
public ResponseEntity<Object> handleSchweineServiceException(SchweineServiceException ex, WebRequest request) {
    ...
    body.put("type", ex.getClass().getSimpleName());// Achtung security
    logger.error("Upps", ex);
    return ResponseEntity.internalServerError().body(body);
}

@ExceptionHandler(AlreadyExistsException.class)
public ResponseEntity<Object> handleAlreadyExistsException(AlreadyExistsException ex, WebRequest request) {
    logger.error("Upps", ex);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
}

@ExceptionHandler(NotFoundException.class)
public ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
    logger.error("Upps", ex);
    return ResponseEntity.notFound().build();
}

@ExceptionHandler(IdMismatchException.class)
public ResponseEntity<Object> handleIdMismatchException(IdMismatchException ex, WebRequest request) {
    logger.error("Upps", ex);
    return ResponseEntity.badRequest().body(ex.getMessage());
}
```

Ein `@ExceptionHandler(XyzException.class)` pro Exception-Typ, jeder
mit einer bewussten Statuscode-Entscheidung. Jetzt, wo alle Schichten
bekannt sind, laesst sich jede Zeile einer Herkunft zuordnen:

- `IdMismatchException` - Presentation Layer (Tag 1,
  `PersonenController`/`SchweinController`).
- `NotFoundException`, `AlreadyExistsException`,
  `PersonenServiceException`, `SchweineServiceException` - Service
  Layer.

Zwei Dinge lohnen eine kurze Diskussion mit den Teilnehmern:

1. **`PersonenServiceException` ist eine geprueft Exception (3.11),
   `SchweineServiceException` dagegen eine ungeprueft** (`extends
   RuntimeException`, siehe
   `service/exception/SchweineServiceException.java`) - fuer
   `@ExceptionHandler` spielt das **keine Rolle**: Ob eine Exception
   checked oder unchecked ist, ist Spring an dieser Stelle egal, es
   zaehlt nur der Typ. Ein guter Kontrast zu 3.11, wo
   checked/unchecked bei `@Transactional(rollbackFor = ...)` sehr
   wohl entscheidend war - und ein guter Vorgriff auf die groessere
   Uebung weiter unten: Warum koennte man sich bei `Schwein` bewusst
   fuer eine ungeprueft Exception entschieden haben?
2. **Der Kommentar `// Achtung security`** neben
   `ex.getClass().getSimpleName()`: Den internen Exception-Typ (oder
   gar `ex.getMessage()` mit technischen Details) ungefiltert an den
   Client zurueckzugeben, kann intern Architektur preisgeben
   (Information Disclosure). Serverseitig wird trotzdem **immer**
   geloggt (`logger.error("Upps", ex)`) - das Prinzip: intern alles
   protokollieren, nach aussen nur das Noetigste zeigen.

> **Diskussionsfrage:** Ein generischer `Exception.class`-Handler als
> letztes Sicherheitsnetz fehlt in diesem `ErrorHandler` bislang. Was
> passiert aktuell bei einer Exception, die keiner dieser fuenf
> Klassen entspricht (z.B. eine unerwartete `NullPointerException`)?
> Ueberlegt euch, ob ein solcher Auffang-Handler ergaenzt werden
> sollte, und was er zurueckgeben muesste, um niemals einen nackten
> Stacktrace an den Client durchzulassen.

### Groessere Uebung: Alle drei Schichten selbst bauen - `Schwein`

Jetzt, wo Persistence, Domain, Service und Fehlerbehandlung komplett
an `Person` durchgespielt sind, wenden die Teilnehmer alles selbst
an: Persistence, Service und REST-Endpoint fuer eine neue Ressource -
`Schwein` - komplett selbst bauen, inklusive der Mapper dazwischen.

#### Aufgabenstellung

Baut fuer `Schwein` denselben Dreiklang, den wir an `Person` bereits
kennengelernt haben - in der Reihenfolge, in der man eine neue
Ressource in der Praxis typischerweise tatsaechlich **baut** (nicht
in der Reihenfolge, in der wir sie bisher **erklaert** haben):

1. **Persistence Layer**: `SchweinEntity` (`@Entity`, `@Id`,
   `@Column`) + `SchweinRepository` (`CrudRepository<SchweinEntity, UUID>`).
2. **Mapper** zwischen Entity und Domain-Modell (MapStruct, siehe
   Tag 2, 3.9).
3. **Domain Layer**: `Schwein` - mit einer wichtigen
   Zusatzanforderung, siehe unten.
4. **Service Layer**: `SchweineService` (Interface) +
   `SchweineServiceImpl` - CRUD wie bei `PersonenService`, plus eine
   fachliche Aktion: **Fuettern**. Jede Fuetterung erhoeht das
   Gewicht des Schweins.
5. **Mapper** zwischen Domain-Modell und DTO.
6. **Presentation Layer**: `SchweinDto` + `SchweinController` -
   CRUD-Endpoints nach denselben Ressourcen-Naming-Regeln wie
   `PersonenController` (Tag 1, 3.1), plus ein Endpoint fuers
   Fuettern.
7. **Fehlerbehandlung erweitern** (3.15/3.16): `SchweineServiceException`
   braucht einen eigenen `@ExceptionHandler`.

**Wichtige Zusatzanforderung an den Domain Layer:** `Person` war
bisher ein reines **Anemic Domain Model** (Martin Fowler) - nur
Felder, Getter, sonst keinerlei Verhalten. Jede Regel steckte im
Service (`validieren`). `Schwein` soll das NICHT sein: Die Regel
"Fuettern erhoeht das Gewicht" gehoert auf das Domain-Objekt selbst
(z.B. eine Methode `schwein.fuettern()`), nicht als
`schwein.setGewicht(schwein.getGewicht() + 1)` im Service. Der
Service ruft nur noch `schwein.fuettern()` auf - er orchestriert
(laden, Methode aufrufen, speichern), er rechnet nicht mehr selbst.

**Zum Fuettern-Endpoint:** Ueberlegt euch, wie sich "Fuettern" mit
den Ressourcen-Naming-Regeln aus Tag 1 vertraegt - es ist offensichtlich
keine der vier Standard-Operationen (Lesen/Anlegen/Aendern/Loeschen)
auf `/v1/schweine/{id}`. Ein Endpoint wie `/v1/schweine/{id}/fuettern`
waere ein Verb in der URL und wuerde gegen die Regel "Ressourcen sind
Nomen" verstossen (siehe [restfulapi.net](https://restfulapi.net/resource-naming/)).
Die gaengige Alternative: Man modelliert die Aktion selbst als
(Unter-)Ressource - eine Fuetterung ist ein Ereignis, das man
**anlegt**: `POST /v1/schweine/{id}/fuetterungen`.

#### Diskussion: In welcher Reihenfolge wuerdet ihr die Klassen bauen?

Bevor es losgeht, lohnt sich ein kurzes Gespraech mit den
Teilnehmern - es gibt nicht DIE eine richtige Reihenfolge, aber gute
Argumente fuer verschiedene:

- **Bottom-up, von der Persistence aus** (wie oben vorgeschlagen):
  Man beginnt beim "Fundament". Jede weitere Schicht hat sofort etwas
  Konkretes, an das sie andocken kann; man kann fruehzeitig gegen
  eine echte (H2-)Datenbank testen. Nachteil: Das API-Design laeuft
  Gefahr, sich zu sehr an der Datenbankstruktur zu orientieren.
- **Top-down / outside-in, vom Controller aus** (Contract-first):
  Man beginnt bei der Frage "Was braucht der Client?" und arbeitet
  sich nach innen vor, zunaechst mit Stubs/Mocks fuer Service und
  Repository. Vorteil: Das API-Design bleibt am Bedarf orientiert.
  Nachteil: Ohne Stubs ist der Code zwischenzeitlich nicht
  compilierbar/lauffaehig.
- **Middle-out, vom Domain-Modell aus**: Man beginnt beim fachlichen
  Kern (`Schwein` inkl. `fuettern()`), ganz ohne Spring, JPA oder
  HTTP - reines Java, isoliert testbar. Danach werden Persistence und
  Presentation als "Adapter" drumherum gebaut. Das ist der
  Kerngedanke hinter Onion-/Hexagonal-Architektur, die wir in diesem
  Kurs bewusst nicht vertiefen - hier reicht der Hinweis, dass diese
  Uebung zeigt, WARUM man so etwas machen wuerde.

Jede dieser Reihenfolgen ist vertretbar - wichtig ist, dass die
Teilnehmer eine bewusste Entscheidung treffen und begruenden koennen.

#### Musterloesung im Projekt

Die fertige Loesung liegt bereits im Projekt (Package `de.fi.webapp`,
jeweils mit `Person`-Pendant zum Vergleich):

| Schicht | Datei |
|---|---|
| Entity | `persistence/entity/SchweinEntity.java` |
| Repository | `persistence/repository/SchweinRepository.java` |
| Mapper (Entity <-> Domain) | `service/mapper/SchweinMapper.java` |
| Domain-Modell | `service/model/Schwein.java` |
| Service-Interface | `service/SchweineService.java` |
| Service-Implementierung | `service/internal/SchweineServiceImpl.java` |
| Mapper (Domain <-> DTO) | `presentation/mapper/SchweinDtoMapper.java` |
| DTO | `presentation/dto/SchweinDto.java` |
| Controller | `presentation/controller/v1/SchweinController.java` |
| Fehlerbehandlung | `presentation/errorhandler/ErrorHandler.java` |

Der entscheidende Ausschnitt - das nicht-anaemische Domain-Modell:

```java
// service/model/Schwein.java
@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schwein {

    private UUID id;
    private String name;
    private int gewicht;

    public void fuettern() {
        setGewicht(getGewicht()  + 1);
    }
}
```

Ein Lombok-Detail, das sich lohnt zu zeigen: `@Setter(AccessLevel.PRIVATE)`
ueberschreibt gezielt, was `@Data` normalerweise erzeugen wuerde
(oeffentliche Setter fuer alle Felder) - hier wird `setGewicht(...)`
bewusst `private` gemacht. Von aussen (auch vom Service!) laesst sich
`gewicht` dadurch NICHT mehr direkt setzen, nur noch ueber
`fuettern()`. Das ist Kapselung im eigentlichen Sinne und macht ein
nicht-anaemisches Domain-Modell erst wirklich wasserdicht - ohne
diesen Kniff koennte jeder Aufrufer die fachliche Regel trotzdem per
`setGewicht(...)` umgehen.

```java
// service/internal/SchweineServiceImpl.java
@Override
public void fuettern(final UUID uuid) {
    try {
        Schwein schwein = repo.findById(uuid)
                .map(mapper::convert)
                .orElseThrow(() -> new NotFoundException("Schwein konnte nicht gefunden werden"));
        schwein.fuettern();
        repo.save(mapper.convert(schwein));
    } catch (NotFoundException e) {
        throw e;
    } catch (RuntimeException e) {
        throw new SchweineServiceException("Fehler beim Fuettern", e);
    }
}
```

Der Service laedt, ruft die fachliche Methode auf dem Domain-Objekt
auf und speichert - die Berechnung selbst ("wie veraendert sich das
Gewicht beim Fuettern?") steht nirgendwo im Service, sondern
ausschliesslich in `Schwein.fuettern()`. Das ist der Unterschied
zwischen einem anaemischen und einem nicht-anaemischen Domain-Modell
in einem Satz.

Zwei weitere Vergleichspunkte zu `Person` lohnen sich direkt am Code:

- `SchweineServiceException` ist bewusst als **ungeprueft** Exception
  angelegt (`extends RuntimeException`) - im Unterschied zu
  `PersonenServiceException` (3.11). `SchweineService` deklariert
  dementsprechend auch kein `throws` an seinen Methoden.
- Genau deshalb reicht `SchweineServiceImpl` ein blankes
  `@Transactional` (Standard-Rollback bei jeder `RuntimeException`
  greift automatisch), waehrend `PersonServiceImpl` explizit
  `@Transactional(rollbackFor = PersonenServiceException.class)`
  braucht (3.11) - der Lehrpunkt aus 3.11 laesst sich damit 1:1 am
  eigenen Code der beiden Services nachvollziehen.

Und die Ergaenzung im `ErrorHandler` (Punkt 7 der Aufgabenstellung,
bereits umgesetzt, siehe 3.16):

```java
@ExceptionHandler(SchweineServiceException.class)
public ResponseEntity<Object> handleSchweineServiceException(SchweineServiceException ex, WebRequest request) {
    ...
    logger.error("Upps", ex);
    return ResponseEntity.internalServerError().body(body);
}
```

### Kapitel 3 (Fortsetzung): REST-Endpoints gegen den laufenden Container testen

#### 3.17 Vom Unit-Test zum Test mit echtem HTTP

Datei: `WebApp/src/test/java/de/fi/webapp/presentation/controller/v1/PersonenControllerTest.java`

```java
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ExtendWith(SpringExtension.class)
@Sql({"/create.sql", "/insert.sql"})
class PersonenControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private PersonenService personenServiceMock;

    @Test
    void findByTest() throws PersonenServiceException {
        final var optionalPerson = Optional.of(new Person(UUID.fromString("b2e24e74-8686-43ea-baff-d9396b4202e0"),"John","Doe"));

        when(personenServiceMock.findeNachId(any())).thenReturn(optionalPerson);

        var personDto = restTemplate.getForObject("/v1/personen/b2e24e74-8686-43ea-baff-d9396b4202e0", PersonDto.class);
        assertEquals("John", personDto.getVorname());
        verify(personenServiceMock, times(1)).findeNachId(UUID.fromString("b2e24e74-8686-43ea-baff-d9396b4202e0"));
    }

    @Test
    void test4() throws PersonenServiceException {
        final Optional<Person> optionalPerson = Optional.empty();
        when(personenServiceMock.findeNachId(any())).thenReturn(optionalPerson);

        var entity = restTemplate.getForEntity("/v1/personen/b2e24e74-8686-43ea-baff-d9396b4202e0", PersonDto.class);
        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }

    @Test
    void test5() throws PersonenServiceException {
        var personen = List.of(
                new Person(UUID.randomUUID(),"John","Doe"),
                new Person(UUID.randomUUID(),"Jane","Doe"));
        when(personenServiceMock.findeAlle()).thenReturn(personen);

        var entity = restTemplate.exchange("/v1/personen", HttpMethod.GET, null, new ParameterizedTypeReference<List<PersonDto>>() { });

        var liste = entity.getBody();
        assertEquals(2, liste.size());
    }
}
```

Im Unterschied zu 3.14 (reiner Mockito-Unit-Test von
`PersonServiceImpl`, **kein** Spring involviert) startet
`@SpringBootTest(webEnvironment = RANDOM_PORT)` diesmal den
**vollstaendigen** Spring-Container inklusive eingebettetem Server auf
einem freien Port - echtes HTTP, echtes Spring MVC-Routing, echte
JSON-(De)Serialisierung.

- `TestRestTemplate` ist ein von Spring Boot bereitgestellter
  HTTP-Client fuer Tests, der Port und Basis-URL des gestarteten
  Containers automatisch kennt. **Versionsdetail:** In der hier
  verwendeten Spring-Boot-Version (4.1, siehe `pom.xml`) reicht
  `@SpringBootTest(webEnvironment = RANDOM_PORT)` allein nicht mehr,
  um `TestRestTemplate` als Bean bereitzustellen - dafuer braucht es
  zusaetzlich `@AutoConfigureTestRestTemplate` (aus dem neuen Package
  `org.springframework.boot.resttestclient`). Ein gutes Beispiel
  dafuer, dass sich auch "Boilerplate-Wegautomatisierung" zwischen
  Framework-Versionen verschieben kann.
- `@MockitoBean` (statt `@Mock`/`@InjectMocks` wie in 3.14) ersetzt
  eine **echte Bean im laufenden ApplicationContext** durch ein
  Mockito-Mock - hier `PersonenService`. Der Controller ist echt, nur
  der Service dahinter wird ausgetauscht. Damit wird gezielt nur die
  **Presentation-Schicht** getestet (Routing, Statuscodes,
  (De-)Serialisierung), ohne echte Business-Logik oder Datenbank.
- `@ActiveProfiles("test")` aktiviert beim Hochfahren das Profil
  `test` (`application-test.properties`, siehe Tag 2) - eigene,
  isolierte Test-Konfiguration mit H2-In-Memory-Datenbank.
- `@Sql({"/create.sql", "/insert.sql"})` laedt vor der Testklasse
  echte Testdaten in diese H2-Datenbank (`src/test/resources/`).
- `test4` zeigt schoen den Bogen zurueck zu Tag 1 (3.1/3.3):
  `ResponseEntity.of(Optional)` liefert bei einem leeren `Optional`
  automatisch `404 Not Found` - genau das laesst sich hier ueber
  einen echten HTTP-Request end-to-end nachweisen.
- `test5` ruft `restTemplate.exchange(...)` mit `null` als
  Request-Body auf - fuer `GET` braucht es keinen Body, anders als
  bei `POST`/`PUT`.

> **Zwei Testebenen, zwei Fragen:** 3.14 beantwortet "Stimmt meine
> Business-Logik?" (ganz ohne Spring, sehr schnell). Dieser Abschnitt
> beantwortet "Kommt bei einem echten HTTP-Request wirklich das
> richtige JSON mit dem richtigen Statuscode heraus?" (mit Spring und
> echtem Server, dafuer langsamer). Beide Ebenen ergaenzen sich, keine
> ersetzt die andere.

> **Diskussionsfrage:** `@Sql({"/create.sql", "/insert.sql"})` laedt
> hier echte Testdaten - obwohl `personenServiceMock` den kompletten
> Service (und damit auch jeden Datenbankzugriff) ersetzt. Braucht
> dieser Test die geladenen Daten ueberhaupt? Was wuerde sich aendern,
> wenn man `PersonenService` NICHT mocken, sondern echt gegen die
> H2-Datenbank laufen lassen wuerde?

### Kapitel 3 (Fortsetzung): Events

#### 3.18 Lose Kopplung durch Publish/Subscribe: `ApplicationEventPublisher`

Bisher hat jede Schicht ihre Nachbarschicht direkt gerufen: Controller
ruft Service, Service ruft Repository. Ein Event dreht dieses Prinzip
um: `PersonServiceImpl` **meldet** nur, dass etwas passiert ist - ohne
zu wissen (oder wissen zu wollen), wer diese Meldung interessiert und
was daraufhin passiert.

Datei: `WebApp/src/main/java/de/fi/webapp/event/PersonCreatedEvent.java`

```java
public record PersonCreatedEvent(UUID id, String vorname, String nachname) {
}
```

Ein Event ist hier nur ein einfaches, unveraenderliches Datenobjekt
(`record`) - anders als in aelteren Spring-Versionen muss es dafuer
NICHT von `ApplicationEvent` erben. Seit Spring 4.2 akzeptiert
`ApplicationEventPublisher.publishEvent(...)` ein beliebiges
POJO/`record` als Event.

Datei: `WebApp/src/main/java/de/fi/webapp/service/internal/PersonServiceImpl.java`

```java
private final ApplicationEventPublisher applicationEventPublisher;

@Override
public void speichern(Person person) throws PersonenServiceException {
    try {
        validieren(person);
        if (repo.existsById(person.getId())) throw new AlreadyExistsException("Datensatz existiert bereits");
        repo.save(mapper.convert(person));
        applicationEventPublisher.publishEvent(new PersonCreatedEvent(person.getId(), person.getVorname(), person.getNachname()));
    } catch (AlreadyExistsException e) {
        throw e;
    } catch (RuntimeException e) {
        throw new PersonenServiceException("Fehler beim Speichern",  e);
    }
}
```

`ApplicationEventPublisher` ist eine von Spring bereitgestellte
Schnittstelle - genau genommen implementiert der `ApplicationContext`
(der Container selbst, siehe Kapitel 2, 2.1) dieses Interface. Dank
`@RequiredArgsConstructor` (Kapitel 2, 2.3) muessen wir dafuer nicht
einmal einen eigenen Konstruktor-Parameter schreiben - das zusaetzliche
`final`-Feld reicht. `publishEvent(...)` wird erst aufgerufen,
NACHDEM `repo.save(...)` erfolgreich war - schlaegt das Speichern
fehl, wird gar kein Event ausgeloest.

Datei: `WebApp/src/main/java/de/fi/webapp/MyEventListener.java`

```java
@Component
public class MyEventListener {

    @EventListener
    public void handlePersonCreatedEvent(PersonCreatedEvent event) {
        System.out.println("PersonCreatedEvent wurde ausgloest");
        System.out.println(event.toString());
    }
}
```

`MyEventListener` ist eine ganz normale `@Component`-Bean (Kapitel 2).
`@EventListener` markiert eine Methode als Empfaenger - Spring
entscheidet allein anhand des **Parametertyps** (`PersonCreatedEvent`),
welche Methode bei welchem Event aufgerufen wird, keine explizite
Registrierung noetig.

> **Merksatz:** `PersonServiceImpl` kennt `MyEventListener` nicht, und
> `MyEventListener` kennt `PersonServiceImpl` nicht - beide kennen nur
> `PersonCreatedEvent`. Das ist das Observer-Pattern, von Spring als
> Publish/Subscribe-Mechanismus fertig bereitgestellt. Im Unterschied
> zu einer direkten Abhaengigkeit (`PersonServiceImpl` haette
> `MyEventListener` per Konstruktor injiziert) lassen sich beliebig
> viele weitere Listener ergaenzen (z.B. eine Mail-Benachrichtigung),
> OHNE `PersonServiceImpl` anzufassen.

> **Diskussionsfrage:** `@EventListener` (ohne weitere Angaben) laeuft
> standardmaessig **synchron und in derselben Transaktion** wie der
> Publisher - `handlePersonCreatedEvent` wird also noch INNERHALB der
> `@Transactional`-Klammer von `speichern()` (3.11) ausgefuehrt. Was
> wuerde passieren, wenn der Listener selbst eine `RuntimeException`
> wirft? Und welchen Unterschied wuerde
> `@TransactionalEventListener(phase = AFTER_COMMIT)` machen?

### Kapitel 3 (Fortsetzung): Komplexe Abfragen und Custom-Repository-Implementierungen

#### 3.19 Zurueck zu Tag 2: `@Query`, Projektionen und die dritte Repository-Strategie

In Tag 2 (3.5/3.6) hatten wir zwei Wege gesehen, wie `PersonenRepository`
zu seinen Abfragen kommt: `CrudRepository` (Standard-Operationen, keine
Zeile eigenen Codes) und die Methodennamen-Konvention (`findByVorname`).
`PersonenRepository` zeigt inzwischen alle drei Strategien gleichzeitig:

```java
public interface PersonenRepository extends CrudRepository<PersonEntity, UUID>, PersonenCustomRepository {

    Iterable<PersonEntity> findByVorname(String vorname);

    @Query("select new de.fi.webapp.persistence.entity.TinyPerson(p.id, p.nachname) from PersonEntity p")
    Iterable<TinyPerson> egal();

    Iterable<TinyPerson> findAllProjectByVorname(String vorname);
}
```

- `egal()` nutzt eigenes JPQL (`@Query(...)`) mit einer sogenannten
  **Constructor-Expression** (`select new ...Entity(...) from ...`):
  Statt ganze `PersonEntity`-Objekte zu laden, baut JPA pro Zeile
  direkt ein `TinyPerson` (ein `record` mit nur `id`/`nachname`, siehe
  `persistence/entity/TinyPerson.java`). Nuetzlich, wenn eine Liste
  nicht jede Spalte braucht - es wird auch nur `id`/`nachname` aus der
  Datenbank gelesen, nicht die komplette Zeile.
- `findAllProjectByVorname(String vorname)` zeigt, dass sich dieselbe
  Projektions-Idee sogar OHNE eigenes `@Query` mit der
  Methodennamen-Konvention (Tag 2, 3.6) kombinieren laesst: Spring Data
  erkennt am Rueckgabetyp (`Iterable<TinyPerson>` statt
  `Iterable<PersonEntity>`), dass hier eine **DTO-Projektion**
  gewuenscht ist, leitet die Filterbedingung wie gewohnt aus dem
  Methodennamen ab (`findBy` + `Vorname`) und baut das Ergebnis
  automatisch ueber den passenden `TinyPerson`-Konstruktor zusammen.

Datei: `WebApp/src/main/java/de/fi/webapp/persistence/repository/PersonenCustomRepository.java`

```java
public interface PersonenCustomRepository {
    void onlySave(PersonEntity personEntity);
}
```

Datei: `WebApp/src/main/java/de/fi/webapp/persistence/repository/PersonenCustomRepositoryImpl.java`

```java
public class PersonenCustomRepositoryImpl implements PersonenCustomRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onlySave(final PersonEntity personEntity) {
        try {
            em.persist(personEntity);
            em.flush();
        } catch (EntityExistsException | DataIntegrityViolationException e) {
            throw new AlreadyExistsException(e.getMessage());
        }
    }
}
```

Manchmal reicht selbst `@Query` nicht mehr aus - z.B. wenn man volle
Kontrolle ueber die JPA-API selbst braucht. Das ist die **dritte**
Repository-Strategie: eine **Custom-Repository-Implementierung**, in
der man ganz normalen Java-Code gegen den `EntityManager` schreibt.

- `@PersistenceContext` injiziert den `EntityManager` - eine weitere
  Auspraegung von Dependency Injection (Kapitel 1), diesmal aber ueber
  die **JPA-Standard-Annotation**, nicht ueber Spring's `@Autowired`
  (funktioniert daher auch ausserhalb von Spring, in jedem
  JPA-Container).
- `em.persist(...)` gefolgt von `em.flush()` erzwingt, dass der
  `INSERT` **sofort** an die Datenbank geschickt wird - anders als
  `save()` von `CrudRepository`, das Hibernate normalerweise erst am
  Ende der Transaktion (beim "Dirty Checking") tatsaechlich
  ausfuehrt. Dadurch laesst sich ein Verstoss gegen einen
  Datenbank-Constraint (z.B. doppelte ID) sofort hier abfangen.
- `@Transactional(propagation = Propagation.REQUIRES_NEW)` startet
  bewusst eine **eigene, neue** Transaktion, unabhaengig von einer
  eventuell schon laufenden - der Gegenpol zu `Propagation.REQUIRED`
  (3.11), das eine laufende Transaktion wiederverwendet.
- `EntityExistsException`/`DataIntegrityViolationException` (technische
  JPA-/Datenbank-Exceptions) werden direkt hier in die fachliche
  `AlreadyExistsException` (3.16) uebersetzt - diesmal schon in der
  **Persistence-Schicht**, nicht erst im Service.

**Wie die drei Strategien zusammenspielen:**

```java
public interface PersonenRepository extends CrudRepository<PersonEntity, UUID>, PersonenCustomRepository {
```

`PersonenRepository` erbt gleichzeitig von `CrudRepository` (Strategie
1: automatischer Proxy zur Laufzeit, Tag 2, 3.5) UND von
`PersonenCustomRepository` (Strategie 3: Handschrift). Spring Data
erkennt allein an der Namenskonvention (`PersonenCustomRepository` +
Suffix `Impl` = `PersonenCustomRepositoryImpl`), dass Aufrufe von
`onlySave(...)` an genau diese handgeschriebene Klasse delegiert
werden sollen - keine explizite Verdrahtung noetig, derselbe
"finde die passende Implementierung automatisch"-Mechanismus wie bei
`CrudRepository` selbst.

`PersonServiceImpl.speichern()` (3.10) ruft entsprechend nicht mehr
`repo.save(...)`, sondern `repo.onlySave(...)`:

```java
repo.onlySave(mapper.convert(person));
applicationEventPublisher.publishEvent(new PersonCreatedEvent(person.getId(), person.getVorname(), person.getNachname()));
```

> **Diskussionsfrage:** Die Uebersetzung von `EntityExistsException`
> in `AlreadyExistsException` passiert hier bereits im Repository
> (`PersonenCustomRepositoryImpl`) - obwohl `PersonServiceImpl.speichern()`
> diesen Fall bisher selbst schon ueber `repo.existsById(...)` vorab
> prueft (3.10). Doppelt gemoppelt, oder sinnvolle Absicherung gegen
> einen Race Condition (zwei gleichzeitige Requests fuer dieselbe ID)?

### Noch offen

Bewusst zurueckgestellt, Themen fuer einen der naechsten Termine:

- **Aspekte (AOP)** (`@Aspect`, `Pointcuts`, `@Before`/
  `@AfterReturning`/`@AfterThrowing`/`@After`/`@Around`) - der
  produktive Nachfolger von `LoggerProxy` (Kapitel 1) und dem
  Proxy-Prinzip hinter Spring Data (Tag 2, 3.5); im Projekt noch
  nicht angelegt.
- **Zweites `@Bean`-Beispiel fuer komplexe Erzeugung aus externen
  Werten** (z.B. YAML via `@PropertySource`/`@ConfigurationProperties`,
  siehe 3.13).
- `OtherRunner` (`@Order`) und `Person.java` (Lombok) aus Kapitel 2 -
  weiterhin vorbereitet, noch nicht im Detail behandelt.
- **Swagger/OpenAPI im Detail** (`@Operation`/`@ApiResponses`,
  bisher nur am Rande in Tag 1 erwaehnt).
