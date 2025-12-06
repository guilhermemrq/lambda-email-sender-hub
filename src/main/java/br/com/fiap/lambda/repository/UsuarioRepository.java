package br.com.fiap.lambda.repository;

import br.com.fiap.lambda.model.Usuario;
import com.amazonaws.services.rdsdata.AWSRDSData;
import com.amazonaws.services.rdsdata.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UsuarioRepository {
    private static final Logger logger = LogManager.getLogger(UsuarioRepository.class);
    
    private final AWSRDSData rdsDataClient;
    private final String clusterArn;
    private final String secretArn;
    private final String database;

    public UsuarioRepository(AWSRDSData rdsDataClient, String clusterArn, String secretArn, String database) {
        this.rdsDataClient = Objects.requireNonNull(rdsDataClient, "RDS Data Client não pode ser nulo");
        this.clusterArn = Objects.requireNonNull(clusterArn, "Cluster ARN não pode ser nulo");
        this.secretArn = Objects.requireNonNull(secretArn, "Secret ARN não pode ser nulo");
        this.database = Objects.requireNonNull(database, "Database não pode ser nulo");
    }

    public List<Usuario> findAllAtivos() {
        logger.info("Buscando administradores ativos");
        
        try {
            ExecuteStatementRequest request = new ExecuteStatementRequest()
                .withResourceArn(clusterArn)
                .withSecretArn(secretArn)
                .withDatabase(database)
                .withSql("SELECT id, name, email, active FROM admin WHERE active = true");
            
            ExecuteStatementResult result = rdsDataClient.executeStatement(request);
            List<Usuario> usuarios = mapResultToUsuarios(result);
            
            logger.info("Encontrados {} administradores ativos", usuarios.size());
            return usuarios;
            
        } catch (Exception e) {
            logger.error("Erro ao buscar administradores: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao buscar administradores do banco de dados", e);
        }
    }

    private List<Usuario> mapResultToUsuarios(ExecuteStatementResult result) {
        List<Usuario> usuarios = new ArrayList<>();
        
        if (result.getRecords() == null || result.getRecords().isEmpty()) {
            logger.warn("Nenhum administrador encontrado no banco de dados");
            return usuarios;
        }
        
        for (List<Field> record : result.getRecords()) {
            try {
                Usuario usuario = new Usuario();
                usuario.setId(record.get(0).getStringValue());
                usuario.setNome(record.get(1).getStringValue());
                usuario.setEmail(record.get(2).getStringValue());
                usuario.setAtivo(record.get(3).getBooleanValue());
                
                usuarios.add(usuario);
                logger.debug("Administrador mapeado: {}", usuario);
                
            } catch (Exception e) {
                logger.error("Erro ao mapear registro de administrador: {}", e.getMessage(), e);
            }
        }
        
        return usuarios;
    }
}
