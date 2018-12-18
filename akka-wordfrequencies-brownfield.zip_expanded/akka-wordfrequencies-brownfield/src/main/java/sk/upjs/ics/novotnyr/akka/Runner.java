package sk.upjs.ics.novotnyr.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Runner {
    public static void main(String[] args) throws Exception {
		ActorSystem system = ActorSystem.create();
		ActorRef master = system.actorOf(Props.create(MasterActor.class));
		
		// sprava prisla zvonku aktoroveho systemu, preto je sender neznamy
		master.tell("The quick brown fox tried to jump over the lazy dog and fell on the dog", ActorRef.noSender());
		master.tell("Dog is man's best friend", ActorRef.noSender());
		master.tell("Dog and Fox belong to the same family", ActorRef.noSender());
		master.tell("The origin of the domestic dog is not clear", ActorRef.noSender());
		
		// posleme spravu o dokonceni posielania
		master.tell(new Eof(), ActorRef.noSender());
		
		// nevieme ako dlho by sme cakali na vypocet, preto sa vysledok neposiela
		// do runnera, ale vypise sa v masterovi
	}
}