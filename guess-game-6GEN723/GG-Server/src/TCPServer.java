import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class TCPServer {
    
    private static final String BIND_IP = "127.0.0.1";
    private static final int PORT = 9090;
    
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        
        try {
            // Création du socket serveur
            InetAddress bindAddress = InetAddress.getByName(BIND_IP);
            serverSocket = new ServerSocket(PORT, 50, bindAddress);
            
            System.out.println("==============================================");
            System.out.println("Serveur TCP démarré sur " + BIND_IP + ":" + PORT);
            System.out.println("En attente de connexions clients...");
            System.out.println("==============================================\n");
            
            // Boucle d'acceptation des clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                // Création d'un thread pour gérer ce client
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
            
        } catch (Exception e) {
            System.err.println("Erreur serveur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Classe interne pour gérer chaque client dans un thread séparé
     */
    private static class ClientHandler implements Runnable {
        
        private Socket clientSocket;
        private Random random;
        
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.random = new Random();
        }
        
        @Override
        public void run() {
            BufferedReader in = null;
            PrintWriter out = null;
            
            try {
                // Obtention des flux d'entrée/sortie
                in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream())
                );
                out = new PrintWriter(
                    clientSocket.getOutputStream(), true
                );
                
                String clientInfo = clientSocket.getInetAddress().getHostAddress() + 
                                   ":" + clientSocket.getPort();
                System.out.println("[CONNEXION] Nouveau client: " + clientInfo);
                
                // Lecture des requêtes du client
                String request;
                while ((request = in.readLine()) != null) {
                    System.out.println("[REÇU] " + clientInfo + " -> " + request);
                    
                    // Traitement de la requête
                    String response = processRequest(request);
                    
                    // Envoi de la réponse
                    out.println(response);
                    System.out.println("[ENVOYÉ] " + clientInfo + " <- " + response);
                }
                
                System.out.println("[DÉCONNEXION] Client: " + clientInfo);
                
            } catch (Exception e) {
                System.err.println("[ERREUR] Client: " + e.getMessage());
            } finally {
                // Fermeture des ressources
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        clientSocket.close();
                        
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        /**
         * Traite une requête selon le protocole REQ/RESP
         * 
         * @param request La requête reçue (format: REQ:CHAINE)
         * @return La réponse formatée (format: RESP:MUTANT_1:MUTANT_2:...:MUTANT_K)
         */
        private String processRequest(String request) {
            // Vérification du format de la requête
            if (request == null || !request.startsWith("REQ:")) {
                return "ERROR:Format invalide. Utilisez REQ:CHAINE_DE_CARACTERES";
            }
            
            // Extraction de la chaîne de caractères
            String originalString = request.substring(4); // Enlève "REQ:"
            
            if (originalString.isEmpty()) {
                return "ERROR:La chaîne ne peut pas être vide";
            }
            
            // Génération d'un nombre aléatoire K entre 1 et 20
            int k = random.nextInt(20) + 1;
            
            // Construction de la réponse avec K mutants
            StringBuilder response = new StringBuilder("RESP");
            
            for (int i = 0; i < k; i++) {
                String mutant = createMutant(originalString);
                response.append(":").append(mutant);
            }
            
            return response.toString();
        }
        
        /**
         * Crée un mutant en modifiant UN caractère aléatoire de la chaîne
         * 
         * @param original La chaîne originale
         * @return La chaîne mutante
         */
        private String createMutant(String original) {
            if (original.length() == 0) {
                return original;
            }
            
            // Conversion en tableau de caractères pour modification
            char[] mutant = original.toCharArray();
            
            // Sélection d'une position aléatoire
            int position = random.nextInt(mutant.length);
            
            // Génération d'un nouveau caractère aléatoire
            // On utilise des caractères imprimables ASCII (33-126)
            char newChar;
            do {
                newChar = (char) (random.nextInt(94) + 33);
            } while (newChar == mutant[position]); // S'assurer que c'est différent
            
            // Remplacement du caractère
            mutant[position] = newChar;
            
            return new String(mutant);
        }
    }
}