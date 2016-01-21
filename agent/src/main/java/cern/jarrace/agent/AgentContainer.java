package cern.jarrace.agent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentContainer {

    private static final Map<Agent, Map<Class<?>, List<Method>>> agents = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Expected two arguments (name host[:port]), but received " + args.length);
        }

        final String name = args[0];
        final String stringUri = args[1];
        try {
            final URL registerUrl = getRegisterUrl(name, stringUri);
            ContainerDiscoverer.discover(agents);
            System.out.println(agents.toString());
            registerServices(registerUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(
                    String.format("Expected URI in form of host[:port], but received %s: %s", stringUri, e));
        }
    }


    public Map<Agent, Map<Class<?>, List<Method>>> getAgents() {
        return agents;
    }

    private static URL getRegisterUrl(String name, String controllerEndpoint) throws MalformedURLException {
        return new URL("http://" + controllerEndpoint + "/jarrace/service/register/" + name);
    }

    private static void registerServices(URL endpoint) {
        agents.entrySet().stream().forEach(entry -> {
            entry.getValue().forEach((clazz, list) ->
                    list.forEach(method -> registerService(endpoint, entry.getKey(), clazz, method)));
        });
    }

    private static void registerService(URL endpoint, Agent agent, Class<?> clazz, Method method) {
        try {
            HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            final String entry = clazz.getName() + " " + method.getName();
            String response = "{\"agent\":\"" + agent.getClass().getName() + "\",\"entry\":\"" + entry + "\"}";
            System.out.println(response);
            connection.getOutputStream().write(response.getBytes());
            System.out.println(connection.getResponseCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
