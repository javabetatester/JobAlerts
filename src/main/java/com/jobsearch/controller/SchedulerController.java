package com.jobsearch.controller;

import com.jobsearch.scheduler.JobSearchScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
@Slf4j
public class SchedulerController {

    private final JobSearchScheduler jobSearchScheduler;

    @PostMapping("/run-now")
    public ResponseEntity<String> runSchedulerNow() {
        try {
            log.info("Executando busca de empregos manualmente via API");
            jobSearchScheduler.searchJobsForAllAlerts();
            return ResponseEntity.ok("Busca de empregos executada com sucesso! Verifique os logs para detalhes.");
        } catch (Exception e) {
            log.error("Erro ao executar busca manual: ", e);
            return ResponseEntity.badRequest()
                    .body("Erro ao executar busca: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<String> getSchedulerStatus() {
        return ResponseEntity.ok("Scheduler está ativo. Executando a cada hora automaticamente.");
    }
}