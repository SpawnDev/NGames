package pkg;

import java.math.BigInteger;

import org.neo.smartcontract.framework.Helper;
import org.neo.smartcontract.framework.SmartContract;
import org.neo.smartcontract.framework.services.neo.Runtime;
import org.neo.smartcontract.framework.services.neo.Storage;

public class NGames extends SmartContract {

    //public static Object Main(byte[] originator, String Event, byte[] args0, byte[] args1, byte[] args2) {
	public static Object Main(String operation, Object[] args) 
	{ 
		byte[] tokenID = Helper.asByteArray("e882be83f854deebd6d2b94fe4f332fe75ad74cbbba9871d5ab8dc9ceb248d53"); // NGT - NGames Token
        byte[] admin = Helper.asByteArray("d920d37d431ae9e053a868de4574f99ce9a48453"); // AbZwbvc4g6jeL1CFUCNQX6nq3k61HgwfdJ
        
		byte[] originator = Helper.asByteArray((String) args[0]);
		byte[] args0 = Helper.asByteArray((String) args[1]);
		byte[] args1 = Helper.asByteArray((String) args[2]);
		byte[] args2 = Helper.asByteArray((String) args[3]);
		Storage.currentContext();
		
    	BigInteger supply = BigInteger.valueOf(1000000);
        String name = "NGames Token";
        String sym = "NGT";
        BigInteger decimals = BigInteger.valueOf(8);
        
        if (!Runtime.checkWitness(originator)) return false;

        if (operation == "totalSupply") return supply;
        if (operation == "name") return name;
        if (operation == "symbol") return sym;
        if (operation == "decimals") return decimals;
        if (operation == "deploy") return Deploy(originator, supply);
        if (operation == "balanceOf") return Storage.get(Storage.currentContext(), args0);
        if (operation == "transfer") return Transfer(originator, args0, args1);
        if (operation == "transferFrom") return TransferFrom(originator, args0, args1, args2);
        if (operation == "approve") return Approve(originator, args0, args1);
        if (operation == "allowance") return Allowance(args0, args1);
        return false;
    }
	
    private static Boolean Deploy(byte[] originator, BigInteger supply) 
    {
        byte[] adminKey = new byte[] {};
        if (originator != adminKey) return false;
        Storage.put(Storage.currentContext(), originator, supply.toByteArray());
        return true;
    }

    private static Boolean Transfer(byte[] originator, byte[] to, byte[] amount) 
    {
        BigInteger originatorValue = new BigInteger(Storage.get(Storage.currentContext(), originator));
        BigInteger targetValue = new BigInteger(Storage.get(Storage.currentContext(), to));

        BigInteger nOriginatorValue = originatorValue.subtract(new BigInteger(amount));
        BigInteger nTargetValue = targetValue.add(new BigInteger(amount));

        if (originatorValue.compareTo(BigInteger.ZERO) != -1 && new BigInteger(amount).compareTo(BigInteger.ZERO) != -1) {
            Storage.put(Storage.currentContext(), originator, nOriginatorValue.toByteArray());
            Storage.put(Storage.currentContext(), to, nTargetValue.toByteArray());
            return true;
        }
        return false;
    }

    private static Boolean TransferFrom(byte[] originator, byte[] from, byte[] to, byte[] amount) 
    {
        BigInteger allVallong = new BigInteger(Storage.get(Storage.currentContext(), Helper.concat(from, to)));
        BigInteger fromVallong = new BigInteger(Storage.get(Storage.currentContext(), from));
        BigInteger toVallong = new BigInteger(Storage.get(Storage.currentContext(), to));

        if (fromVallong.compareTo(BigInteger.ZERO) != -1 && new BigInteger(amount).compareTo(BigInteger.ZERO) != -1 &&
            allVallong.compareTo(BigInteger.ZERO) != -1) {
            Storage.put(Storage.currentContext(), Helper.concat(from, originator), (allVallong.subtract(new BigInteger(amount))).toByteArray());
            Storage.put(Storage.currentContext(), to, (toVallong.add(new BigInteger(amount))).toByteArray());
            Storage.put(Storage.currentContext(), from, (fromVallong.subtract(new BigInteger(amount))).toByteArray());
            return true;
        }
        return false;
    }

    private static Boolean Approve(byte[] originator, byte[] to, byte[] amount) 
    {
        Storage.put(Storage.currentContext(), Helper.concat(originator, to), amount);
        return true;
    }
    
    private static BigInteger Allowance(byte[] from, byte[] to) 
    {
        return new BigInteger(Storage.get(Storage.currentContext(), Helper.concat(from, to)));
    }
}