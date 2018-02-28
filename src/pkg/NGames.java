package pkg;

import java.math.BigInteger;

import org.neo.smartcontract.framework.Helper;
import org.neo.smartcontract.framework.SmartContract;
import org.neo.smartcontract.framework.services.neo.Account;
import org.neo.smartcontract.framework.services.neo.Blockchain;
import org.neo.smartcontract.framework.services.neo.Header;
import org.neo.smartcontract.framework.services.neo.Runtime;
import org.neo.smartcontract.framework.services.neo.Storage;

public class NGames extends SmartContract 
{
	public static Object Main(String operation, Object[] args) 
	{ 
		byte[] tokenID = Helper.asByteArray("e882be83f854deebd6d2b94fe4f332fe75ad74cbbba9871d5ab8dc9ceb248d53"); // NGT - NGames Token
        byte[] owner = Helper.asByteArray("d920d37d431ae9e053a868de4574f99ce9a48453"); // AbZwbvc4g6jeL1CFUCNQX6nq3k61HgwfdJ
        
		byte[] invoker = Helper.asByteArray((String) args[0]);
		byte[] args1 = Helper.asByteArray((String) args[1]);
		byte[] args2 = Helper.asByteArray((String) args[2]);
		byte[] args3 = Helper.asByteArray((String) args[3]);
		byte[] args4 = Helper.asByteArray((String) args[4]);
		byte[] args5 = Helper.asByteArray((String) args[5]);
		int currentTime = Blockchain.getHeader(Blockchain.height()).timestamp(); //Long.valueOf(String.valueOf(int))
        // BigInteger blockTime = BigInteger.valueOf(currentTime); // can't cast from long to int when compiling with neoj.exe
		
    	BigInteger supply = BigInteger.valueOf(1000000);
        String name = "NGames Token";
        String sym = "NGT";
        BigInteger decimals = BigInteger.valueOf(8);

        
        if (!Runtime.checkWitness(invoker)) return false;

        if (operation == "totalSupply") return supply; //OK
        if (operation == "name") return name; //OK
        if (operation == "symbol") return sym; //OK
        if (operation == "decimals") return decimals; //OK
        if (operation == "createGame") return CreateGame(invoker, args1, args2, args3, args4, args5); //OK
        if (operation == "joinGame") return JoinGame(invoker, args1, args2, args3, args4); // gameID, Tokens(buyIn+deposit), buyIn, deposit
        if (operation == "playerForfeit") return PlayerForfeit(invoker, args1); // gameID, Tokens(buyIn+deposit), buyIn, deposit
        if (operation == "cancelGame") return CancelGame(invoker, args1); //OK
        
        if (operation == "balanceOf") return Storage.get(Storage.currentContext(), args1);
        if (operation == "deploy") return Deploy(invoker, supply); //OK
        if (operation == "transfer") return Transfer(invoker, args1, args2);
        if (operation == "approve") return Approve(invoker, args1, args2);
        return false;
    }
    private static Boolean CreateGame(byte[] invoker, byte[] gameID, byte[] players, byte[] buyIn, byte[] deposit, byte[] expiryDate) 
    {
    	Storage.put(Storage.currentContext(), "admin", invoker);
    	Storage.put(Storage.currentContext(), "gameID", Helper.asString(gameID));
    	Storage.put(Storage.currentContext(), "players", Helper.asBigInteger(players));
    	Storage.put(Storage.currentContext(), "buyIn", Helper.asBigInteger(buyIn));
    	Storage.put(Storage.currentContext(), "deposit", Helper.asBigInteger(deposit));
       	Storage.put(Storage.currentContext(), "expiryDate", Helper.asBigInteger(expiryDate));
       	Storage.put(Storage.currentContext(), "gameState", "gameStarted");
        return true;
    }
    private static Boolean JoinGame(byte[] invoker, byte[] gameID, byte[] tokens, byte[] buyIn, byte[] deposit) 
    {
    	BigInteger forPrize = Helper.asBigInteger(buyIn);
    	BigInteger security = Helper.asBigInteger(deposit);
    	long currentTime = Long.valueOf(String.valueOf(Blockchain.getHeader(Blockchain.height()).timestamp())).longValue();
    	
    	if (Helper.asBigInteger(tokens).equals(forPrize.add(security)))
    	{
    		if (BigInteger.valueOf(currentTime).compareTo(Helper.asBigInteger(Storage.get(Storage.currentContext(), "expiryDate"))) < 0)
    		{
            	Storage.put(Storage.currentContext(), "player", invoker);
            	Storage.put(Storage.currentContext(), "gameID", Helper.asString(gameID));
            	Storage.put(Storage.currentContext(), "buyIn", Helper.asBigInteger(buyIn));
            	Storage.put(Storage.currentContext(), "deposit", Helper.asBigInteger(deposit));
                return true;
    		}
    	}
    	return false;
    }
    private static Boolean PlayerForfeit(byte[] invoker, byte[] gameID)
    {
    	byte[] owner = Helper.asByteArray("d920d37d431ae9e053a868de4574f99ce9a48453");
    	if (Storage.get(Storage.currentContext(), "gameID").equals(gameID))
    	{
    		return Transfer(owner, invoker, Storage.get(Storage.currentContext(), "deposit"));
    	}
      return false;
    }
    private static Boolean CancelGame(byte[] invoker, byte[] gameID) 
    {
        long currentTime = new Header().timestamp();
        
        
    	if (Storage.get(Storage.currentContext(), "gameID").equals(gameID))
    	{
        	if (Storage.get(Storage.currentContext(), "admin").equals(invoker))
        	{
        		if (BigInteger.valueOf(currentTime).compareTo(Helper.asBigInteger(Storage.get(Storage.currentContext(), "expiryDate"))) < 0)
        		{
        			Storage.put(Storage.currentContext(), "gameState", "gameCanceled");
        			return true;
        		}
        	}
    	}
    	return false;
    }

    private static Boolean Deploy(byte[] invoker, BigInteger supply) 
    {
        byte[] adminKey = Helper.asByteArray("d920d37d431ae9e053a868de4574f99ce9a48453");
        if (invoker != adminKey) return false;
        Storage.put(Storage.currentContext(), invoker, supply.toByteArray());
        return true;
    }
    private static Boolean Transfer(byte[] invoker, byte[] to, byte[] amount) 
    {
        BigInteger originatorValue = new BigInteger(Storage.get(Storage.currentContext(), invoker));
        BigInteger targetValue = new BigInteger(Storage.get(Storage.currentContext(), to));

        BigInteger nOriginatorValue = originatorValue.subtract(new BigInteger(amount));
        BigInteger nTargetValue = targetValue.add(new BigInteger(amount));

        if (originatorValue.compareTo(BigInteger.ZERO) != -1 && new BigInteger(amount).compareTo(BigInteger.ZERO) != -1) 
        {
            Storage.put(Storage.currentContext(), invoker, nOriginatorValue.toByteArray());
            Storage.put(Storage.currentContext(), to, nTargetValue.toByteArray());
            return true;
        }
        return false;
    }

    private static Boolean Approve(byte[] invoker, byte[] to, byte[] amount) 
    {
        Storage.put(Storage.currentContext(), Helper.concat(invoker, to), amount);
        return true;
    }
}