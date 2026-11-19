package br.uff.banco.shared.web;

import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serve o build do Angular pela própria API, quando {@code banco.frontend.diretorio} aponta para
 * ele (variável {@code BANCO_FRONTEND_DIRETORIO}; é o que a imagem Docker faz). Front e API no mesmo
 * domínio: sem CORS e sem um servidor web a mais.
 * <p>
 * Rotas do Angular (ex.: {@code /contas/5/extrato}) não são arquivos: devolvem o {@code index.html}
 * e o roteador do front resolve. Caminhos da API e do actuator nunca caem nesse fallback — um
 * {@code /api/...} inexistente continua sendo 404 da API.
 * <p>
 * Em desenvolvimento a propriedade não existe e o front roda no {@code ng serve} (proxy para a API).
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty("banco.frontend.diretorio")
public class FrontendConfig implements WebMvcConfigurer {

    private final String diretorio;

    FrontendConfig(Environment ambiente) {
        String configurado = ambiente.getRequiredProperty("banco.frontend.diretorio");
        this.diretorio = configurado.endsWith("/") ? configurado : configurado + "/";
    }

    /** A raiz não chega ao resolvedor (o handler de recursos recusa caminho vazio): vai direto ao index. */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("file:" + diretorio)
                .resourceChain(false)
                .addResolver(new ArquivoOuIndex());
    }

    /** Arquivo existente (JS, CSS, ícone...) ou, para rotas do front, o {@code index.html}. */
    static class ArquivoOuIndex extends PathResourceResolver {

        @Override
        protected Resource getResource(String caminho, Resource local) throws IOException {
            Resource arquivo = local.createRelative(caminho);
            if (arquivo.exists() && arquivo.isReadable()) {
                return arquivo;
            }
            if (caminho.startsWith("api/") || caminho.startsWith("actuator/") || caminho.contains(".")) {
                return null; // 404 de verdade: API, actuator ou arquivo estático inexistente
            }
            return local.createRelative("index.html");
        }
    }
}
