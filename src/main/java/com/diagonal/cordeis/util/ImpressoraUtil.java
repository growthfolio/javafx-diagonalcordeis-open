package com.diagonal.cordeis.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.PrinterState;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImpressoraUtil {

    private static final Logger logger = Logger.getLogger(ImpressoraUtil.class.getName());

    public static void imprimirPdf(String caminhoArquivoPdf, String nomeImpressora) throws Exception {
        logger.log(Level.INFO, "Iniciando impressão do arquivo: {0} na impressora: {1}", new Object[]{caminhoArquivoPdf, nomeImpressora});

        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService impressoraEscolhida = null;

        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(nomeImpressora)) {
                impressoraEscolhida = service;
                break;
            }
        }

        if (impressoraEscolhida == null) {
            String msg = String.format("Impressora '%s' não encontrada ou não disponível.", nomeImpressora);
            logger.severe(msg);
            throw new Exception(msg);
        }

        // Verificar estado da impressora (se suportado pelo driver)
        try {
            PrinterState state = impressoraEscolhida.getAttribute(PrinterState.class);
            if (state != null && state == PrinterState.STOPPED) {
                String msg = String.format("A impressora '%s' está parada (STOPPED).", nomeImpressora);
                logger.warning(msg);
                throw new Exception(msg);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Não foi possível verificar o estado da impressora ''{0}''. Ignorando verificação. Motivo: {1}", new Object[]{nomeImpressora, e.getMessage()});
        }

        PDDocument document = PDDocument.load(new File(caminhoArquivoPdf));
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPageable(new PDFPageable(document));
        job.setPrintService(impressoraEscolhida);

        try {
            logger.log(Level.INFO, "Enviando trabalho de impressão para a impressora ''{0}''.", nomeImpressora);
            job.print();
            logger.log(Level.INFO, "Impressão concluída com sucesso.");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Erro ao imprimir o documento: {0}", ex.getMessage());
            throw new Exception("Erro ao imprimir o documento: " + ex.getMessage(), ex);
        } finally {
            document.close();
            logger.fine("Documento PDF fechado após tentativa de impressão.");
        }
    }

    public static List<String> listarImpressorasDisponiveis() {
        logger.info("Listando impressoras disponíveis...");

        List<String> impressoras = new ArrayList<>();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

        if (services.length == 0) {
            logger.warning("Nenhuma impressora encontrada. Verifique se o CUPS está ativo ou se há impressoras instaladas.");
        } else {
            logger.info("Impressoras disponíveis:");
            for (PrintService service : services) {
                String nome = service.getName();
                logger.log(Level.INFO, " - {0}", nome);
                impressoras.add(nome);
            }
        }

        return impressoras;
    }
}
