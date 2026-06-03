# Šah

## O programu

Ovaj program predstavlja implementaciju klasične igre šaha. Podržava kompletna pravila igre, uključujući specijalne poteze kao što su rokada, en passant i promocija pešaka.

Tok partije se beleži kroz istoriju poteza, a nakon završetka igre moguće je pregledati celu partiju kroz detaljan prikaz poteza. Implementiran je i sistem tajmera, uz mogućnost izbora različitih trajanja za partiju.

Kada se program upali potrebno je izabrati trajanje partije. Nakon toga, figure se pomeraju klikom na polje sa kojeg se pomeraju, a zatim na jedno od dozvoljenih polja.

### Demonstracija partije
https://github.com/matf-pp/2026_Sah/blob/main/docs/simple_game.gif
### Demonstracija korišćenja istorije
Na kraju partije moguće je pogledati kako je tabla izgledala u određenom trenutku partije klikom na potez sa leve strane:

https://github.com/matf-pp/2026_Sah/blob/main/docs/move_history.gif

##  Korišćene tehnologije

* Kotlin
* Compose Multiplatform
* Gradle build sistem
* IntelliJ IDEA

---

## Potrebni alati za pokretanje

* Java **21 (Temurin ili OpenJDK 21)**
* Linux desktop okruženje

---

## Prevodjenje aplikacije iz izvornog koda 

1. Klonirati repozitorijum sledećom komandom:

```bash
git clone https://github.com/matf-pp/2026_Sah.git
cd 2026_Sah
```

2. U root direktorijumu projekta:

```bash
chmod +x gradlew
./gradlew clean run
```

> Napomena: Ukoliko nije podešena ispravna verzija JDK-a, gradle će izbaciti grešku


## Pokretanje isporučenog izvršnog programa

1. Preuzeti Chess.zip fajl sa Releases taba
2. Otpakovati arhivu.
3. Pokrenuti program komandom: 

```bash
./Chess/bin/Chess
```

---

## Operativni sistem

* Izvršni fajl je preveden za Linux operativni sistem

---
## Autori

* Petar Pešić — email: [petar.pesic04@gmail.com](mailto:petar.pesic04@gmail.com)
* Tadija Matić — email: [tadijamatic0807@gmail.com](mailto:tadijamatic0807@gmail.com)

---

