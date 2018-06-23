Progetto 'Casa+ Tracking'
=========================

Descrizione
-----------

Realizzazione di un'applicazione che ha il compito di effettuare il tracking di persone affette da sindrome di Down.

### Caratteristiche

* Login con scelta tra impronta digitale e immissione dati (nome utente, numero telefono).
* Men&ugrave; preferenze con dati utente configurabile (nome, numero telefono, numero telefono educatore, numero telefono emergenza) e parametri di sistema.
* Geolocalizzazione con percorsi ed aree sicure utilizzando le Google Maps API.
* Alert a utente con segnale sonoro e vibrazione in caso di abbandono di un'area sicura, popup con possibilit&agrave; di chiamata all'educatore e invio (a scelta e non) di un SMS contenente il link alla posizione dell'utente su Google Maps.
* Chiamata ed SMS con la posizione utente automatiche in caso di assenza di interazioni entro 15 secondi.
* Chiamata ed SMS con la posizione utente disponibili in quasiasi momento premendo l'apposito pulsante SOS.
* Possibilit&agrave; di scattare foto da utilizzare successivamente come immagine di un POI (Point Of Interest).
* Avvio automatico dell'app, con conseguente geolocalizzazione utente, attraverso notifiche dati Firebase.
* Server che si occupa di salvare l'associazione TokenID - UserID, ricevuti dall'app installata sui vari client, in un database MySQL. Tale server dispone di un'interfaccia web con logica PHP/JS con la quale &egrave; possibile inviare una notifica dati Firebase al client corrispondente allo UserID.

### Struttura del progetto

Il progetto sar&agrave; sviluppato con l'IDE [Android Studio](https://developer.android.com/studio/index.html).  
Inoltre si utilizzer&agrave; il sistema di notifiche [Firebase](https://firebase.google.com/) per attivare l'app sul telefono del client e consentirne la geolocalizzazione.  
Per altre informazioni sul sistema si visiti il [Server](http://www.smartengineers.eu/joomla/) di riferimento dell'app.

### Contatti (e-mail) 

**Riccardo, 229032:** [riccardoarmando.diprinzio@student.univaq.it](mailto:riccardoarmando.diprinzio@student.univaq.it)
