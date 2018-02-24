package pkg;

import org.neo.smartcontract.framework.SmartContract;
import org.neo.smartcontract.framework.services.neo.Blockchain;
import org.neo.smartcontract.framework.services.neo.Runtime;
import org.neo.smartcontract.framework.services.neo.Storage;

public class NPoker extends SmartContract 
{
    public static Object Main(String operation, Object[] args) 
    {
    	int i = args.length;
    	Runtime.notify(i);
        String gameID = (String) args[0];

        if (operation == "NewGame") 
        {
            return NewGame(gameID);
        }

        if (operation == "JoinGame") 
        {
            return JoinGame(gameID, (byte[]) args[1]);
        }

        if (operation == "ForfeitGame") 
        {
            return ForfeitGame(gameID, (byte[]) args[1]);
        }

        if (operation == "EndGame") 
        {
            return EndGame(gameID);
        }

        if (operation == "CancelGame") 
        {
            return CancelGame(gameID);
        }
        
        if (operation == "CheckGame") 
        {
            return CheckGame(gameID);
        }
        return false;
    }
    
    private static byte[] NewGame(String domain) 
    {
        return Storage.get(Storage.currentContext(), domain);
    }
    private static boolean JoinGame(String domain, byte[] owner) 
    {
        if (!Runtime.checkWitness(owner)) 
        {
            return false;
        }

        byte[] value = Storage.get(Storage.currentContext(), domain);
        if (value != null) 
        {
            return false;
        }

        Storage.put(Storage.currentContext(), domain, owner);
        return true;
    }
    private static boolean ForfeitGame(String domain, byte[] to) 
    {
        if (!Runtime.checkWitness(to)) 
        {
            return false;
        }

        byte[] from = Storage.get(Storage.currentContext(), domain);

        if (from == null) 
        {
            return false;
        }

        if (!Runtime.checkWitness(from)) 
        {
            return false;
        }

        Storage.put(Storage.currentContext(), domain, to);
        return true;
    }
    private static boolean EndGame(String domain) 
    {
        byte[] owner = Storage.get(Storage.currentContext(), domain);

        if (owner == null) 
        {
            return false;
        }

        if (!Runtime.checkWitness(owner)) 
        {
            return false;
        }

        Storage.delete(Storage.currentContext(), domain);
        return true;
    }
    private static boolean CancelGame(String domain) 
    {
        byte[] owner = Storage.get(Storage.currentContext(), domain);

        if (owner == null) 
        {
            return false;
        }

        if (!Runtime.checkWitness(owner)) 
        {
            return false;
        }

        Storage.delete(Storage.currentContext(), domain);
        return true;
    }
    private static byte[] CheckGame(String domain) 
    {
        return Storage.get(Storage.currentContext(), domain);
    }
}