# Riepilogo Awesome Pizza — pratica + ragionamento (da colloquio)

Questo documento non è solo un inventario delle modifiche fatte sulla chat; serve a capire **perché** quelle scelte hanno senso, dove sono i compromessi, e come parlarne in modo professionale senza suonare “a elenco tecnico”. Lo leggo così come me lo leggerebbe un interviewer: cerco coerenza, trade-off honesti e intenzione progettuale.

---

## 1) Allineamento con la traccia (e cosa manca davvero)

La traccia chiedeva sostanzialmente un portale prima iterazione **solo API**: creare ordini, vedere uno stato lungo una macchina a stati, avere una coda lato pizzaiolo, niente registrazione utente, Spring + Java 17+, test.

Il progetto copre bene questa idea: c’è un contratto REST reale, validazione in ingresso, persistenza JPA, flusso di stato lineare, codice ordine pubblico e query per cliente e “operativo” separati anche solo per semantica (code vs id internal).

Quello che resta più “dibattibile”, e conviene nominarlo esplicitamente, è che alcune assunzioni della traccia (un pizzaiolo reale che lavora in serie) nel codice oggi sono solo **culturali**, non ancora imposte dall’implementazione: ad esempio si possono avere più ordini in `IN_PROGRESS` se qualcuno avvia più id diversi.

Quindi la frase forte da fare in sede tecnica è: “La traccia permette libertà progettuale — io ho prioritizzato consegna e chiarezza, ma nella prossima iterazione rinforzerei l’invariante di dominio con una regola transazionale o un endpoint esplicito di ‘take next’.”

Sull’H2 in-memory: non è “sbagliato” per una prima iterazione; è una scelta di **velocità e semplicità operativa**. Il costo è noto: niente continuità dati tra restart. In produzione non lo confonderei con “abbiamo già il database”: lì servirebbe un engine managed, migrazioni (Flyway/Liquibase) e backup.

---

## 2) H2: cosa significa davvero per te e per chi intervista

H2 qui non è “il database del prodotto finale”. È un componente che ti permette di sviluppare come se avessi un DB reale, con SQL e JPA, senza dover tenere in piedi un servizio esterno. È utile perché:

- elimina frizione per chi clona il repo;
- rende i test e l’avvio locale riproducibili;
- ti obbliga comunque a modellare correttamente relazioni e transazioni.

Il trade-off principale è la **durabilità**. Se nella demo dici che “persistiamo su database”, sarebbe più preciso dire “persistiamo con JPA; in dev uso H2 in-memory per pragmaticità”.

---

## 3) README: perché vale la pena allinearlo (e cosa comunica sul candidato)

Un README fuori sincrono con gli endpoint e i path Swagger sembra più grave di codice migliorabile: suggerisce mancanza di cura sulla developer experience.

L’allineamento che abbiamo fatto non è “cosmesi”: è un segnale che capisci che **il contratto** (URL, versioning, Swagger) è parte del delivery. Nel colloquio puoi anche dire una cosa semplice ma matura:

> “Tratto la doc come parte del sistema: deve essere verificabile, non aspirazionale.”

---

## 4) Docker e Kubernetes: intenzione e limiti consapevoli

Il `Dockerfile` multi-stage ha un’idea precisa: separare la build (Maven, dipendenze, compilazione) dall’immagine runtime (solo JRE + jar). Non è obbligatorio per la traccia, ma risponde a una domanda che spesso arriva: “come lo porti in un ambiente controllato?”.

La struttura `kustomize base + overlays` non è glamour: è pragmatica quando vuoi dichiarare “questo cambia ambiente” (namespace, repliche, tag immagine, variabili) senza forkare YAML interi. È anche un modo limpido da difendere: “Preferisco overlays a duplicazioni massiccie; aggiusto solo le parti che divergono.”

Due note critiche serie che è bene avere sulla punta delle dita:

- i placeholder tipo `ghcr.io/your-org/...` vanno bene come template, ma chi intervista noterà che **non sono ancora collegati a una CI reale**;
- readiness/liveness ora potrebbe puntare a un endpoint che concettualmente non è un health check “puro” (una lista ordini non è ideale come probe se un giorno richiede auth o diventa costosa). In produzione normalmente introdurrei `actuator/health` e stringere le probe.

---

## 5) Config business negli overlay: perché non è solo “spostamento YAML”

Quando hai chiesto di mettere massimi pizze/quantità negli overlay, l’idea sottostante è: **policy configurabile per contesto**.

In locale puoi essere permissivo o stringente solo per demo; negli ambienti più vicini alla produzione capisci perché quel limite cambia davvero: stress test, SLA, comportamento antisbrocco, limiti sul payload.

La scelta tecnica di usare variabili d’ambiente e binding Spring (`application.yaml` con `${VAR:default}`) è standard e difendibile: non devi cambiare codice per cambiare limiti.

La cautela è questa:

- nei ConfigMap Kubernetes i valori sono stringhe ma tipicamente vanno bene per numeri interpolati;
- in produzione spesso preferisci **feature flags** o limiti governati da config service, ma per questa scala va bene.

---

## 6) Logging: da “testo libero” a qualcosa che si può cercare davvero

Prima avevi già correlation id sulla request grazie al filtro HTTP e al pattern Logback (`%X{requestId}`). Questo livello è la base dell’observabilità pragmatica.

La richiesta di uniformare eventi funzionali con `orderId`, `orderCode`, `status` nasce dal fatto che, quando fai troubleshooting in produzione, non ti basta leggere una frase: ti serve **struttura** che i log aggregator possono filtrare o aggregare senza parsing fragile.

Nel colloquio la frase naturale è:

> “Ho provato a tenere una separazione ragionevole tra log tecnici ingresso HTTP nel controller e eventi dominio nel service, con formato coerente per poterli correlare al client tramite request id e al business tramite order code.”

Trade-off onesto: se metti troppi eventi a `INFO`, il volume cresce. Spesso in maturità si introduce sampling o si sposta parte in `DEBUG` con policy per ambiente.

---

## 7) Errori rossi in IDE con Lombok: non confondere “IDE” con “compilatore”

Questa è una scena classica: l’IDE mostra decine di errori su `log`, builder, getter, mentre Maven compila.

Non è un mistero: l’IDE non sta applicando il processor Lombok oppure la toolchain Java dell’editor non combacia con il setup.

In colloquio non serve dramma: puoi essere secco:

> “Il build è la fonte di verità della CI; quando l’IDE disallinea, sistemo annotation processing/versione JDK e Maven import; non modifico codice solo per placare falsi positivi.”

Mostra anche maturità: capisci la differenza tra **static analysis IDE** e **javac+maven**.

---

## 8) “Configuration”: cosa stai comprando quando metti quel package

Riassumo in modo più discorsivo.

`AuditingConfig` risponde alla domanda: “Chi ha modificato questo record e quando?”. Qui non c’è utente vero quindi `"system"` è un compromesso accettabile ma da raccontare come temporaneo: è consapevolezza, non ingenuità.

`RequestIdFilter` risponde alla domanda: “Come ricostruisco il percorso di una richiesta attraverso micro-layer e log?”. È un pattern elementare ma solido: input header, output header, MDC, cleanup in `finally`.

`AwesomePizzaProperties` risponde alla domanda: “I limiti di business sono cablati o governabili?”. Usare configuration properties + validazione evita che la logica diventi un labirinto di costanti nel service.

---

## 9) “Utils”: piccoli pezzi, grande impatto se usati con disciplina

`LogContextUtils` è “tecnico purissimo”: sa solo di MDC e identificatori. È corretto che non sia Spring bean: non ha stato, non ha lifecycle.

`OrderLogUtils` è “semantica di log”: mette ordine al narrare business. Il rischio classico è che diventi un mega-dump: qui l’obiettivo era standardizzazione, non encyclopedia.

`ValidateUtils` ripete pattern guard clause. Una critica gentile ma sensata è: l’eccezione usata dovrebbe idealmente essere più specifica quando non è stato ordine (`IllegalArgumentException` / `BusinessRuleException`). Puoi essere tu a proporre evoluzione: mostra judgement.

---

## 10) Exceptions: perché `@ControllerAdvice` non è overhead ma design

Nel web layer hai scelto di tradurre eccezioni del dominio in HTTP con payload uniforme (`ErrorResponse`).

Nel colloquio la cosa che impressiona positivamente è riconoscere i margini:

- `409 CONFLICT` su violazione stato è ragionabile, ma alcuni team preferiscono `422` o errori applicativi strutturati;
- `MethodArgumentNotValidException` in `400` è standard;
- il catch generico `Exception` come rete di sicurezza va bene se logghi stacktrace ma non dovrebbe essere la strategia principale quando il codice matura.

In pratica direi: centralizzazione sì, ma evolverei verso codici errore stabili nel tempo per client.

---

## 11) Constant / Controller / Model: layering “semplice ma coerente”

`ApiPaths` è minuscolo ma risolve una cosa irritante nei progetti medi: dispersione stringhe degli URL nei test.

L’idea `OrderApi + OrderController implements` comunica chiaramente dove vive il contratto e dove l’implementazione delega.

Il package `model` mescola entity JPA e DTO. Per la dimensione va bene e ti fa andare veloce. Se mi chiedessero dove migliorerei, direi splitting package per non confondere “persistenza DB” con “contratto pubblico”: non è filosofia ma riduzione degli errori di import e delle dipendenze accidentali.

---

## 12) Service: questo è dove di solito chiedono “ma perché hai fatto così?”

Nel service tieni invarianti dominio (`validateOrderRequest`), transizioni stato, uso repository, orchestrazione mapper.

Due messaggi chiave:

1) **`@Transactional` a livello classe** è pragmatico ma non gratuitamente “free”: dovresti essere consapevole che ogni pubblic method apre ambito transazionale di default salvo `@Transactional(readOnly=true)` dove lo metti sul metodo più specifico (in Spring vale la granularità più specifica sugli overload di meta-annotation nel modo atteso dalla versione: qui l’idea generale comunque è dichiarare intenzioni read-only).

2) **Perché `createOrder` non delega direttamente a `OrderMapper.toEntity`**. Questo è importante: nella create vuoi decidere tu cosa accetta il mondo esterno. Un mapper “copia tutto dal DTO all’entity” rischia accidentalmente di mappare id/codice/stato che non dovrebbero essere controllabili dal cliente. Una create esplicita o un mapper dedicato `toNewOrder(...)` comunica maturità.

---

## 13) Test: dove sei forte e cosa migliorerei senza farsi prendere dall’impostore syndrome

Nel service hai test con Mockito perché vuoi velocità e controllo sulla collaborazione (`when`/`verify`): è la scelta giusta quando la logica sta lì dentro.

Nel controller hai MockMvc standalone e mock del service perché vuoi isolare binder/validazioni/status HTTP e perché hai collegato `GlobalExceptionHandler`: è una mossa più realistica per errori `@Valid`.

Gli `@SpringBootTest` “contextLoads” sono utili ma sottili: non dimostrano end-to-end; dimostrano “il wiring non è rotto drasticamente”. In colloquio puoi essere onesto senza abbassarti: “Smoke test sul contesto; aggiungerei test più stretti su persistence e API full-stack quando il dominio cresce.”

---

## 14) Annotazioni JPA sulle entity: non imparare a memoria, imparare gli effetti collaterali

Qui conta capire più che elencare.

`cascade ALL` sulla relazione vuol dire responsabilità: se sbaglio il model object graph, Hibernate ti fa cascare conseguenze anche di delete.

`EAGER` sulle pizze vuol dire comodità oggi, possibile N+1 o payload pesanti domani: dimostra che capisci la differenza eager/lazy quando parliamo di volumi reali.

`orphanRemoval` è potente quando vuoi davvero “togli dall’insieme vuol dire cancella dalla tabella”: va spiegato con calma, perché accidentalmente può cancellare più di quel che sembra nel mental model MVC.

Audit via Spring Data auditing risolve metadati comuni bene, ma quando introduci sicurezza reale dovresti puntare auditing “vero”, non sempre `system`.

---

## 15) Secrets e overlay

La linea migliore resta netta:

- ConfigMap non è per segreti;
- segreti in manifest separati o tooling di encryption/external secret store;
- in GKE/production la domanda vera è rotazione e accesso, non “dove sta la stringa YAML”.

---

## 16) Stato del lavoro al termine della chat (cosa puoi affermare con serenità)

Puoi dire con serenità che hai:

- un backend coerente con una prima iterazione API della traccia;
- documentazione aggiornata rispetto al codice;
- un percorso di packaging e distribuzione dichiarativo (Docker + Kustomize) con consapevolezza dei punti placeholder;
- policy business esternalizzabili tramite configurazione/cluster;
- logging con correlation id ed eventi business più cercabili;
- test esistenti ragionati e non accidentalmente “solo perché maven lo chiedeva”.

E puoi essere altrettanto chiaro su cosa faresti dopo (invarianti col pizzaiolo, cancellazioni, actuator health, migrazioni DB, test di integrazione più profondi). Non è arretratezza — è progettazione iterativa.
