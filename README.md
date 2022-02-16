# LocateReborn
For your BCA stalking needs.

# Setup
clone it
./gradlew run to run, ./gradlew build to build (might want to clear out build/distributions every so often and before making a distribution)

# Generate data
once it's possible there'll be a POST route, but for now just go into Server.kt and put
```java
downloadData()
```
at the top of main
