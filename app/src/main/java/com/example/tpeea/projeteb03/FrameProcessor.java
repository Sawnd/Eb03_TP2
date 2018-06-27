package com.example.tpeea.projeteb03;

public class FrameProcessor {

/**Permet de transformer un payload reçu en paramètre en une trame compréhensible apr l'oscilloscope**/
    // On reçoit en paramètres un tableau de byte contenant la commande et ses arguments
    public byte[] toFrame(byte[] commande) {
        // On construit la trame
        byte header = 0x05;
        byte tail = 0x04;
        // length indique la taille de la commande
        byte[] length = {0x00, (byte) commande.length};
        //byte[] payload = toEchap(commande);
        int sum = length[1] + toSumTab(commande);
        // ctrl se fait avant echappement
        byte ctrl = (byte) (Integer.parseInt(toComplement2(Integer.toHexString(sum)), 16));
        byte[] preEchap = new byte[length.length + commande.length + 1];
        preEchap[0] = length[0];
        preEchap[1] = length[1];
        int j = 2;
        for (byte b : commande) {
            preEchap[j] = b;
            j++;
        }
        preEchap[j] = ctrl;

        byte[] payload = toEchap(preEchap);
        byte[] frame = new byte[2 + payload.length];


        // On assemble tous les éléments de la frame pour la construire
        frame[0] = header;
        int i = 1;
        for (byte b : payload) {
            frame[i] = b;
            i++;
        }
        frame[i] = tail;

        return frame;
    }

    // Renvoie le complement à deux d'un hexadecimal
    String toComplement2(String hex) {
        int i = Integer.parseInt(hex, 16);
        i = i % 256;
        int result = 256 + ~i + 1;

        return Integer.toHexString(result);

    }

// Permet de sommer les octets contenu dans un tableau de byte
    int toSumTab(byte[] tab) {
        int total = 0;
        for (int val : tab) {
            total += (int) val;
        }
        return total;
    }

    // permet d'inserer un caractère d'echappemet devant les bytes réservés , ici 0x04,0x05,0x06. Rajoute 0x06 au carcatère suivant et renvoie le tableau de byte échappé
    byte[] toEchap(byte[] b) {
        int i = 0;
        for (byte bi : b) {
            if (bi == 0x06 || bi == 0x04 || bi == 0x05) {
                i++;
            }
        }
        byte[] result = new byte[b.length + i];
        i = 0;
        for (byte bi : b) {

            if (bi == 0x06 || bi == 0x04 || bi == 0x05) {
                result[i] = 0x06;
                result[i + 1] = (byte) (bi + 0x06);
                i += 2;
            } else {
                result[i] = bi;
                i++;
            }
        }
        return result;
    }
  /**Permet d'enlever les caractères d'échappements d'une trame**/
    byte[] toUnechap(byte[] b) {
        int i = 0;
        for (byte bi : b) {
            if (bi == 0x06) {
                i++;
            }
        }
        byte[] result = new byte[b.length - i];
        int j = 0;
        for (int k = 0; k < b.length; k++) {

            if (b[k] == 0x06) {

                result[j] = (byte) (b[k + 1] - 0x06);
                j++;
                k++;
            } else {
                result[j] = b[k];
                j++;
            }
            if (j == result.length) {
                break;
            }


        }
        return result;
    }

    public byte[] fromFrame(byte[] trameFromOscillo) {
        int stockDebut=0;
        int stockFin=0;
        byte[] resultWith0x06=new byte[1024];
        byte[] resultWithout0x06=new byte[1024];
        for(int i=0;i<trameFromOscillo.length;i++){
            if(trameFromOscillo[i]==(byte)0x8F) {
                stockDebut=i;

            }
            if(trameFromOscillo[i]==(byte)0x04) {
                stockFin=i;
                break;
            }

        }
        int size=stockFin-stockDebut;
        for (int j=0;j<size-1;j++){
            resultWith0x06[j]=trameFromOscillo[stockDebut+1];
            stockDebut++;
        }
        resultWithout0x06=toUnechap(resultWith0x06);
        return resultWithout0x06;
    }

}
