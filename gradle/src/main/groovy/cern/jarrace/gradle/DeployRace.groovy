package cern.jarrace.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.regex.Pattern

class DeployRace implements Plugin<Project> {

    void apply(Project project) {

        project.task('jarDeploy', type : Jar) {
            description = "Creates a deployable jar"

            baseName = project.name + '-all'
            // Include all compile dependencies (fatjar)
            from project.configurations.compile.collect { it.isDirectory() ? it : project.zipTree(it) }
            // Include all source code for this project
            from project.sourceSets.main.allSource
        }

        project.task('deployRace').dependsOn('classes', 'jarDeploy') << {

            Pattern testPattern = Pattern.compile("@Test");
            String fileList = project.sourceSets.main.allSource.filter { File file ->
                file.find { line -> testPattern.matcher(line).find() } != null
            }.collect { File file ->
                String name = file.toString()
                String shortHand = name.substring(name.indexOf("java") + 5, name.length() - 5)
                shortHand.replace("/", ".")
            }.join(',')

            String jarName = project.name + '-all'
            Path path = Paths.get("$project.buildDir/libs/" + jarName + ".jar")
            InputStream input = Files.newInputStream(path, StandardOpenOption.READ)


            String headerList = "testServices"
            URL url = new URL("http://localhost:8080/jarrace/container/deploy/gradletask")
            HttpURLConnection connection = (HttpURLConnection) url.openConnection()
            connection.setRequestMethod("POST")
            connection.setRequestProperty("Content-Type", "application/octet-stream")
            connection.setRequestProperty(headerList, fileList)
            connection.setDoOutput(true)
            OutputStream output = connection.getOutputStream()

            int bufferSize = 4096
            byte[] buffer = new byte[bufferSize]
            int bytesRead = 0
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead)
            }
            input.close()
            output.close()

            System.out.println(connection.getResponseCode())
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))
            System.out.println(reader.readLine())

        }
    }
}