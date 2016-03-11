import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MTGDck2CodConverter {
  private final static int NAME_START_INDEX = 5;
  
  private String fileName;
  
  public static void main(String... args) {
    if(args[0] == null) {
      System.out.println("Please specify a .dck file to convert.");
    } else {
      MTGDck2CodConverter converter = new MTGDck2CodConverter(args[0]);
    }
  }
  
  public MTGDck2CodConverter(String fileName) {
    this.fileName = fileName;
    
    init();
  }
  
  private void init() {
    Deck deck = new Deck();
    
    try (BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)))) {
      Zone main = new Zone("main");
      Zone sideBoard = new Zone("sideboard");

      String line;
      while((line = reader.readLine()) != null) {
        if(line.startsWith("NAME:")) {
          deck.setName(line.substring(NAME_START_INDEX));
        } else if(line.startsWith("SB:")) {
          Card card = new Card();
          card.setNumber(Integer.valueOf(line.substring(4, line.indexOf('[') - 1)));
          card.setPrice(0);
          card.setName(line.substring(line.indexOf(']') + 2));
          
          sideBoard.getCards().add(card);
        } else {
          Card card = new Card();
          card.setNumber(Integer.valueOf(line.substring(0, line.indexOf('[') - 1)));
          card.setPrice(0);
          card.setName(line.substring(line.indexOf(']') + 2));

          main.getCards().add(card);
        }
      }
      
      deck.setMain(main);
      deck.setSideBoard(sideBoard);
      
      System.out.println("Success!");
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    generateCodFile(deck);
  }
  
  private void generateCodFile(Deck deck) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder;
    
    try {
      docBuilder = factory.newDocumentBuilder();
      Document doc = docBuilder.newDocument();
      
      Element rootElement = doc.createElement("cockatrice_deck");
      
      Attr version = doc.createAttribute("version");
      version.setValue("1");
      rootElement.setAttributeNode(version);
      
      doc.appendChild(rootElement);
      
      Element deckName = doc.createElement("deckname");
      rootElement.appendChild(deckName);
      
      Element comments = doc.createElement("comments");
      rootElement.appendChild(comments);
      
      Element mainZone = doc.createElement("zone");
      
      Attr mainZoneName = doc.createAttribute("name");
      mainZoneName.setValue("main");
      mainZone.setAttributeNode(mainZoneName);
      
      for(Card mainDeckCard : deck.getMain().getCards()) {
        Element card = doc.createElement("card");
        
        Attr cardNumber = doc.createAttribute("number");
        cardNumber.setValue(String.valueOf(mainDeckCard.getNumber()));
        card.setAttributeNode(cardNumber);
        
        Attr cardPrice = doc.createAttribute("price");
        cardPrice.setValue("0");
        card.setAttributeNode(cardPrice);
        
        Attr cardName = doc.createAttribute("name");
        cardName.setValue(mainDeckCard.getName());
        card.setAttributeNode(cardName);
        
        mainZone.appendChild(card);
      }
      
      rootElement.appendChild(mainZone);

      Element sideZone = doc.createElement("zone");

      Attr sideZoneName = doc.createAttribute("name");
      sideZoneName.setValue("side");
      sideZone.setAttributeNode(sideZoneName);

      for(Card sideBoardCard : deck.getSideBoard().getCards()) {
        Element card = doc.createElement("card");

        Attr cardNumber = doc.createAttribute("number");
        cardNumber.setValue(String.valueOf(sideBoardCard.getNumber()));
        card.setAttributeNode(cardNumber);

        Attr cardPrice = doc.createAttribute("price");
        cardPrice.setValue("0");
        card.setAttributeNode(cardPrice);

        Attr cardName = doc.createAttribute("name");
        cardName.setValue(sideBoardCard.getName());
        card.setAttributeNode(cardName);

        sideZone.appendChild(card);
      }

      rootElement.appendChild(sideZone);
      
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(new File(deck.getName().replaceAll(" ", "_") + ".cod"));
      
      transformer.transform(source, result);
      
      System.out.println("Done!");
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  private class Deck {
    private String name;
    private Zone main;
    private Zone sideBoard;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Zone getMain() {
      return main;
    }

    public void setMain(Zone main) {
      this.main = main;
    }

    public Zone getSideBoard() {
      return sideBoard;
    }

    public void setSideBoard(Zone sideBoard) {
      this.sideBoard = sideBoard;
    }
  }
  
  private class Zone {
    private String name;
    private List<Card> cards;
    
    public Zone(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public List<Card> getCards() {
      if(cards == null) {
        cards = new ArrayList<Card>();
      }
      
      return cards;
    }
  }
  
  private class Card {
    private int number;
    private int price;
    private String name;

    public int getNumber() {
      return number;
    }

    public void setNumber(int number) {
      this.number = number;
    }

    public int getPrice() {
      return price;
    }

    public void setPrice(int price) {
      this.price = price;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
