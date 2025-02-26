# Social_Network

Proiectul reprezintă o aplicație Java pentru o rețea socială de tip Facebook, rulând cu interfață grafică JavaFX și bază de date PostgreSQL. Scopul aplicației este să permită utilizatorilor să se înregistreze, să se conecteze, să-și adauge prieteni, să trimită mesaje private și să vizualizeze chat-uri.

------------------------------------------------------------------------------
## Caracteristici principale

- Autentificare utilizatori cu parole criptate (BCrypt).
- Gestiunea conturilor de utilizator (creare, ștergere, actualizare).
- Gestionarea prieteniilor (adăugare, acceptare, respingere, ștergere).
- Trimiterea și recepționarea de mesaje (inclusiv răspuns “reply” la un mesaj anterior).
- Vizualizarea și paginarea listei de prieteni în interfața grafică.
- Notificări despre cererile de prietenie în așteptare.

------------------------------------------------------------------------------
## Cerințe de sistem

- Java 17+.
- PostgreSQL (instalat și configurat la adresa și datele de conectare corespunzătoare).  
  Implicit, proiectul este configurat să se conecteze la:
  - URL: jdbc:postgresql://localhost:5432/postgres
  - Utilizator DB: stefansandru
  - Parola DB: 1234

Dacă doriți să folosiți alte date de conectare sau altă bază de date, trebuie ajustate valorile din cod (de exemplu, în dbUserRepo, dbFriendshipRepo și MessageRepo).

------------------------------------------------------------------------------
## Structura proiectului

1. Fisierele Java principale se află în pachetul com.example.social_network:
   - HelloApplication.java – Clasa de start pentru aplicația JavaFX.
   - MainController.java – Controller principal, gestionează interfața (listarea prietenilor, căutări, etc.).
   - LoginController.java – Controller pentru ecranul de login.
   - ChatController.java – Controller pentru fereastra de chat (afișare mesaje, trimitere de mesaje, reply etc.).
   - MultipleMessage.java – Exemplu de controller pentru trimiterea de mesaje către mai mulți destinatari (dacă este implementat).

2. Pachetul Service:
   - SocialNetworkService.java – Logica principală a aplicației (adăugare prietenii, trimitere mesaje, afișare date etc.).

3. Pachetul Repo (Repository Pattern):
   - dbUserRepo.java – Interacțiune cu tabelul „users”.
   - dbFriendshipRepo.java – Interacțiune cu tabelul „friendships”.
   - MessageRepo.java – Interacțiune cu tabela „messages” și relația dintre mesaje și destinatari.

4. Pachetul domain (Model):
   - User.java, Friendship.java, Message.java, Tuple.java etc.
   - Reprezintă entitățile din baza de date și datele pe care le gestionează aplicația.

5. Pachetul util:
   - PasswordUtil.java – Funcționalități de criptare și verificare a parolelor (folosește BCrypt).

6. Resurse JavaFX (SceneBuilder):
   - FXML-uri pentru ecrane (login-view.fxml, main-view.fxml, chat-view.fxml etc.).
   - Fișiere CSS/stiluri (style.css).

------------------------------------------------------------------------------
## Bază de date

Este nevoie să existe următoarele tabele (sau echivalente) în PostgreSQL:

- users  
  Coloane recomandate (ID bigint primary key, name text, password text, profile_image_path text, etc.)

- friendships  
  Coloane (id1, id2, f_date timestamp, status text). Unde id1 și id2 sunt ID-urile utilizatorilor care formează o prietenie.

- messages  
  Coloane (id bigint primary key, from_user_id bigint, message text, date timestamp, reply_to bigint [opțional pentru reply]).  

- message_recipients  
  (message_id bigint, to_user_id bigint), pentru a marca destinatarii legați de fiecare mesaj.

Notă: Script-urile de creare a acestor tabele nu sunt incluse direct în proiect. Va trebui să configurați manual schema în baza de date.

------------------------------------------------------------------------------
## Pași pentru rulare

1. Clonați proiectul în mediul vostru local (sau descărcați-l arhivat).
2. Asigurați-vă că PostgreSQL rulează și ați actualizat conexiunea la baza de date în fișierele dbUserRepo, dbFriendshipRepo și MessageRepo (dacă e nevoie de alți parametri).
3. Deschideți un terminal în directorul principal al proiectului.
4. Rulați comanda pentru încărcarea wrapper-ului Gradle și compilare:
   
   pe Linux/Mac:
   » ./gradlew build
   
   pe Windows:
   » gradlew.bat build
5. Pentru a porni aplicația:
   pe Linux/Mac:
   » ./gradlew run
   
   pe Windows:
   » gradlew.bat run

6. Se va deschide fereastra de login JavaFX. Introduceți un ID de utilizator valid (ex.: 1, 2, 3) și parola aferentă.

------------------------------------------------------------------------------
## Funcționalități în interfață

- Login: Introduceți ID și parolă. Verificarea are loc prin comparare criptată cu ce există în baza de date.
- În fereastra principală:
  - Vă puteți afișa lista de prieteni, cu opțiuni de paginare (Next / Previous).
  - Se afișează butoane pentru “Unfriend”, “Chat”.  
  - Puteți căuta persoane necunoscute (care nu sunt în lista de prieteni) și le puteți trimite cereri de prietenie.
  - Veți vedea cereri de prietenie în așteptare și puteți accepta sau respinge.
- Chat: Trimite mesaje individuale către un utilizator. Se pot face reply la un mesaj anterior pentru a continua conversația.

------------------------------------------------------------------------------
## Dezvoltare și extensii

- Adăugați validări suplimentare sau reguli personalizate în FriendshipValidator.java și UserValidator.java.
- Puteți extinde interfața grafică cu mai multe scene (FXMLE) pentru profil utilizator, setări, etc.
- Puteți introduce testare automată folosind JUnit (framework inclus).

------------------------------------------------------------------------------
## Contribuții / Copyright

Acesta este un proiect demo/educațional. Pentru întrebări sau contribuții, puteți sincroniza un fork al acestui repository.  

------------------------------------------------------------------------------
## Suport

Pentru erori sau întrebări legate de proiect:

1. Verificați dacă rularea cu Java 17+ rezolvă problemele de compatibilitate.
2. Asigurați-vă că baza de date PostgreSQL este configurată corect și rulează.
3. În cazul mesajelor de eroare SQL, verificați tabelele și drepturile utilizatorului BD.
4. Dacă întâmpinați erori la compilare, asigurați-vă că toate dependențele indică mavenCentral() și că aveți conexiune la internet.

------------------------------------------------------------------------------

© 2023 Proiect demonstrativ – Social_Network.  