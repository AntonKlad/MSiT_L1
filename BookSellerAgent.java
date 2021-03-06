import jade.core.Agent;
import jade.core.behaviours.*;
import java.util.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class BookSellerAgent extends Agent {
    // The catalogue of books for sale (maps the title of a book to its price)
    private Hashtable catalogue;
    // The GUI by means of which the user can add books in the catalogue
    private BookSellerGui myGui;

    protected void setup(){
        // Create the catalogue
        catalogue = new Hashtable();
        // Create and show the GUI
        myGui = new BookSellerGui(this);
        myGui.show();

        // Add the behaviour serving requests for offer from buyer agents
        addBehaviour(new OfferRequestsServer());
        // Add the behaviour serving purchase orders from buyer agents
        addBehaviour(new PurchaseOrdersServer());
    }

    // Put agent clean-up operations here
    protected void takeDown(){
        // Close the GUI
        myGui.dispose();
        // Printout a dismissal message
        System.out.println("Seller-agent "+getAID().getName()+ " terminating.");
    }

    /**
     This is invoked by the GUI when the user adds a new book for sale
     */
    public void updateCatalogue(final String title, final int price) {
        addBehaviour(new OneShotBehaviour(){
            public void action(){
                catalogue.put(title, new Integer(price));
            }
        });
    }

    /**
     Inner class OfferRequestsServer.
     This is the behaviour used by Book-seller agents to serve incoming requests
     for offer from buyer agents.
     If the requested book is in the local catalogue the seller agent replies
     with a PROPOSE message specifying the price. Otherwise a REFUSE message is
     sent back. */
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action(){
            ACLMessage msg = myAgent.receive();
            if (msg != null){
                // Message received. Process it
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = (Integer) catalogue.get(title);
                if (price != null){
                    // The requested book is available for sale. Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                }else{
                    // The requested book is NOT available for sale.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
        }
    }// End of inner class OfferRequestsServer




    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action(){
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null){
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer price = (Integer) catalogue.remove(title);
                if (price != null){
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(title+" sold to agent "+msg.getSender().getName());
                }else{
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }else{
                block();
            }
        }

    }

}