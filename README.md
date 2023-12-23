# CrazyAdvancementsAPI


## About

CrazyAdvancementsAPI is an API for creating and managing Advancements programmatically on Minecraft Spigot Servers


## Maven
First, add the Jitpack Repository to your repositories
```xml
<repositories>
  <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
  </repository>
</repositories>
```
Then, add the following to your dependencies:
```xml
<dependency>
    <groupId>com.github.ZockerAxel</groupId>
    <artifactId>CrazyAdvancementsAPI</artifactId>
    <version>VERSION</version>
</dependency>
```
Replace VERSION with the Version you want, for example for v2.1.17, you would use:
```xml
<dependency>
    <groupId>com.github.ZockerAxel</groupId>
    <artifactId>CrazyAdvancementsAPI</artifactId>
    <version>v2.1.17a</version>
</dependency>
```

## Gradle
First, add the Jitpack Repository to your repositories
```kts
repositories {
    maven { url 'https://jitpack.io' }
}
```
Then, add the following to your dependencies:
```kts
dependencies {
    compileOnly("com.github.ZockerAxel:CrazyAdvancementsAPI:{VERSION}")
}
```
Or for mojang-mapped jar:
```kts
dependencies {
    compileOnly("com.github.ZockerAxel:CrazyAdvancementsAPI:{VERSION}:mojmap")
}
```

And replace VERSION with the Version you want, for exmaple for v2.1.17, you would use:
```kts
dependencies {
    compileOnly("com.github.ZockerAxel:CrazyAdvancementsAPI:{v2.1.17}")
}
```



## Documentation

The Official Documentation can be found [here][0]

There is also Javadoc available [here][1]

[0]: https://docs.crazyadvancements.endercentral.eu "Official Documentation"
[1]: https://javadoc.crazyadvancements.endercentral.eu "Javadoc"
