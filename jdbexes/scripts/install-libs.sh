# bin/sh

mvn install:install-file -DrepositoryId=lib -Dfile="lib/chars-0.0.1-SNAPSHOT.jar" -DgroupId=jplag -DartifactId=chars -Dversion="0.0.1-SNAPSHOT" -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -DrepositoryId=lib -Dfile="lib/scheme-0.0.1-SNAPSHOT.jar" -DgroupId=jplag -DartifactId=scheme -Dversion="0.0.1-SNAPSHOT" -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -DrepositoryId=lib -Dfile="lib/frontend-utils-0.0.3-SNAPSHOT.jar" -DgroupId=jplag -DartifactId=frontend-utils -Dversion="0.0.3-SNAPSHOT" -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -DrepositoryId=lib -Dfile="lib/utils-0.0.3-SNAPSHOT.jar" -DgroupId=jplag -DartifactId=utils -Dversion="0.0.3-SNAPSHOT" -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -DrepositoryId=lib -Dfile="lib/cpp-0.0.2-SNAPSHOT.jar" -DgroupId=jplag -DartifactId=cpp -Dversion="0.0.2-SNAPSHOT" -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -DrepositoryId=lib -Dfile="lib/jplag-2.11.9-SNAPSHOT.jar" -DgroupId=jplag -DartifactId=jplag -Dversion="2.11.9-SNAPSHOT" -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -DrepositoryId=lib -Dfile="lib/text-0.0.1-SNAPSHOT.jar" -DgroupId=jplag -DartifactId=text -Dversion="0.0.1-SNAPSHOT" -Dpackaging=jar -DgeneratePom=true
