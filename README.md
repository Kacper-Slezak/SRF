System Rekomendacji Filmów (SRF)
SRF to aplikacja służąca do rekomendowania filmów na podstawie algorytmu dekompozycji macierzy (SVD - Singular Value Decomposition). System umożliwia dynamiczne zarządzanie preferencjami użytkowników, oferując rekomendacje filmowe oparte na wprowadzonych ocenach oraz przeszukiwanie bazy danych z możliwością otwierania linków do IMDb.

Funkcjonalności
- Rekomendacje filmów: Aplikacja sugeruje filmy na podstawie algorytmu SVD, wykorzystując oceny wprowadzone przez użytkownika.
- Ocena filmów: Użytkownicy mogą oceniać filmy, co wpływa na generowane rekomendacje.
- Przeszukiwanie bazy danych: Intuicyjne wyszukiwanie filmów w bazie danych.
- Linki do IMDb: Bezpośrednie otwieranie stron filmów w serwisie IMDb.

Struktura projektu
Projekt jest zorganizowany w następujący sposób:

->src/main/java/com/srf

--> app: Główna część aplikacji, w której znajduje się klasa uruchomieniowa.
controllers: Klasy kontrolerów zarządzających logiką aplikacji. Odpowiadają za obsługę interakcji użytkownika.

--> dao: Warstwa dostępu do danych, odpowiedzialna za interakcję z bazą danych.

-->models: Definicje modeli danych wykorzystywanych w aplikacji, takich jak filmy czy użytkownicy.

-->services: Logika biznesowa aplikacji, implementacja algorytmu SVD oraz innych funkcji związanych 
z rekomendacjami.

-->utils: Narzędzia pomocnicze, np. klasy do obsługi danych czy logowania.

->src/main/resources

-->com/srf: Dodatkowe zasoby używane w aplikacji, np. pliki konfiguracyjne.

-->db: Zasoby bazy danych, takie jak pliki CSV lub inne dane źródłowe.

-->uml: Diagramy UML przedstawiające strukturę i działanie aplikacji.
