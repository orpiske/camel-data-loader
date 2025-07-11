
1. `export PREVIOUS_VERSION=0.0.9 CURRENT_DEVELOPMENT_VERSION=1.0.0 NEXT_DEVELOPMENT_VERSION=1.1.0`
2. `mvn -Pdist clean package`
3. `mvn release:clean`
4. `mvn --batch-mode -Dtag=camel-data-loader-${CURRENT_DEVELOPMENT_VERSION} release:prepare -DreleaseVersion=${CURRENT_DEVELOPMENT_VERSION} -DdevelopmentVersion=${NEXT_DEVELOPMENT_VERSION}-SNAPSHOT`
5. `mvn -Pdist release:perform -Dgoals=install`
6. `git checkout camel-data-loader-${CURRENT_DEVELOPMENT_VERSION} && jreleaser full-release -Djreleaser.project.version=${CURRENT_DEVELOPMENT_VERSION}`