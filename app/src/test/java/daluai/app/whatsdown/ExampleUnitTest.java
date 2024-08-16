package daluai.app.whatsdown;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    private static class SampleListener implements ServiceListener {
        private List<ServiceInfo> services = new ArrayList<>();

        @Override
        public void serviceAdded(ServiceEvent event) {
            System.out.println("Service added: " + event.getInfo());
            services.add(event.getInfo());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            System.out.println("Service removed: " + event.getInfo());
            services.remove(event.getInfo());
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            System.out.println("Service resolved: " + event.getInfo());
        }

        public List<ServiceInfo> getServices() {
            return services;
        }
    }

    @Test
    public void dns() throws IOException, InterruptedException {
        JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

        // Add a service listener
        SampleListener listener = new SampleListener();
        jmdns.addServiceListener("_http._tcp.local.", listener); // Listening for HTTP services

        // Wait a bit to collect services
        Thread.sleep(5000);

        // List all discovered services
        System.out.println("Discovered services:");
        for (ServiceInfo serviceInfo : listener.getServices()) {
            System.out.println(serviceInfo);
        }

        // Close the JmDNS instance
        jmdns.close();
    }
}