import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;


public class TCPClient {
    
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9090;
    
    public static void main(String[] args) {
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Création et connexion du socket
            socket = new Socket();
            socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
            
            System.out.println("==============================================");
            System.out.println("Client TCP connecté au serveur");
            System.out.println("Serveur: " + SERVER_IP + ":" + SERVER_PORT);
            System.out.println("==============================================\n");
            
            // Obtention des flux d'entrée/sortie
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true
            );
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            
            // Boucle d'interaction avec l'utilisateur
            // Le client reste ouvert jusqu'à ce que l'utilisateur tape 'quit'
            boolean running = true;
            while (running) {
                System.out.print("Entrez votre texte sans 'REQ:' (ou 'quit' pour quitter): ");
                String userInput = scanner.nextLine();
                
                // Vérification de sortie
                if (userInput.equalsIgnoreCase("quit")) {
                    System.out.println("Déconnexion...");
                    running = false;
                    continue;
                }
                
                // Protection : si l'utilisateur tape "REQ:" par erreur, on l'enlève
                if (userInput.toUpperCase().startsWith("REQ:")) {
                    System.out.println("⚠️  Note : Pas besoin de taper 'REQ:', le client l'ajoute automatiquement.");
                    userInput = userInput.substring(4); // Enlever "REQ:"
                }
                
                // Construction de la requête
                String request = "REQ:" + userInput;
                
                // Envoi de la requête
                out.println(request);
                System.out.println("[ENVOYÉ] " + request);
                
                // Réception de la réponse
                String response = in.readLine();
                
                if (response == null) {
                    System.out.println("⚠️  Connexion fermée par le serveur.");
                    running = false;
                    continue;
                }
                
                System.out.println("[REÇU] " + response);
                
                // Affichage formaté des mutants
                displayFormattedResponse(response, userInput);
                System.out.println();
            }
            
            System.out.println("✓ Client TCP fermé proprement.");
            
        } catch (Exception e) {
            System.err.println("Erreur client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fermeture des ressources
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                scanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Affiche la réponse du serveur de manière formatée
     * 
     * @param response La réponse complète du serveur
     * @param original La chaîne originale envoyée
     */
    private static void displayFormattedResponse(String response, String original) {
        // Vérification du format de la réponse
        if (response.startsWith("ERROR:")) {
            System.out.println("\n❌ ERREUR: " + response.substring(6));
            return;
        }
        
        if (!response.startsWith("RESP:")) {
            System.out.println("\n⚠️  Format de réponse invalide: " + response);
            return;
        }
        
        // Extraction des mutants
        String[] parts = response.split(":");
        
        if (parts.length < 2) {
            System.out.println("\n⚠️  Aucun mutant reçu");
            return;
        }
        
        // Affichage formaté
        System.out.println("\n✓ Réponse du serveur:");
        System.out.println("  Chaîne originale: \"" + original + "\"");
        System.out.println("  Nombre de mutants: " + (parts.length - 1));
        System.out.println("  Mutants générés:");
        
        for (int i = 1; i < parts.length; i++) {
            System.out.println("    " + i + ". \"" + parts[i] + "\" " + 
                             highlightDifference(original, parts[i]));
        }
    }
    
    /**
     * Met en évidence la différence entre la chaîne originale et le mutant
     * 
     * @param original La chaîne originale
     * @param mutant Le mutant
     * @return Une chaîne indiquant la position de la mutation
     */
    private static String highlightDifference(String original, String mutant) {
        if (original.length() != mutant.length()) {
            return "";
        }
        
        for (int i = 0; i < original.length(); i++) {
            if (original.charAt(i) != mutant.charAt(i)) {
                return "(position " + i + ": '" + original.charAt(i) + 
                       "' → '" + mutant.charAt(i) + "')";
            }
        }
        
        return "(aucune différence détectée)";
    }
}