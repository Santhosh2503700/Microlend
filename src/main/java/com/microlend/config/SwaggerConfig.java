package com.microlend.config;
 
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI microlendOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()

                .info(new Info()
                        .title("MicroLend API")
                        .version("1.0")
                        .description("""
<div style="font-family:Arial; font-size:13px; line-height:1.8">

<span style="background:#eef2f7; padding:4px 10px; border-radius:12px;">Admin Login</span>
→
<span style="background:#f5f7fa; padding:4px 10px; border-radius:12px;">Setup Products</span>
→
<span style="background:#eef2f7; padding:4px 10px; border-radius:12px;">Staff Setup</span>
→
<span style="background:#f5f7fa; padding:4px 10px; border-radius:12px;">Field Onboarding</span>
→
<span style="background:#eef2f7; padding:4px 10px; border-radius:12px;">Borrower Creation</span>
→
<span style="background:#f5f7fa; padding:4px 10px; border-radius:12px;">KYC Upload</span>
→
<span style="background:#eef2f7; padding:4px 10px; border-radius:12px;">KYC Verification</span>
→
<span style="background:#f5f7fa; padding:4px 10px; border-radius:12px;">Loan Application</span>
→
<span style="background:#eef2f7; padding:4px 10px; border-radius:12px;">Credit Assessment</span>
→
<span style="background:#f5f7fa; padding:4px 10px; border-radius:12px;">Approval</span>
→
<span style="background:#eef2f7; padding:4px 10px; border-radius:12px;">Sanction</span>
→
<span style="background:#f5f7fa; padding:4px 10px; border-radius:12px;">Acceptance</span>
→
<span style="background:#eef2f7; padding:4px 10px; border-radius:12px;">Disbursement</span>
→
<span style="background:#f5f7fa; padding:4px 10px; border-radius:12px;">Repayment Schedule</span>
→
<span style="background:#eef2f7; padding:4px 10px; border-radius:12px;">EMI Collection</span>
→
<span style="background:#f5f7fa; padding:4px 10px; border-radius:12px;">Payments</span>
→
<span style="background:#eef2f7; padding:4px 10px; border-radius:12px;">Delinquency</span>
→
<span style="background:#f5f7fa; padding:4px 10px; border-radius:12px;">Collections</span>
→
<span style="background:#eef2f7; padding:4px 10px; border-radius:12px;">Reports</span>
→
<span style="background:#f5f7fa; padding:4px 10px; border-radius:12px;">Notifications</span>
→
<span style="background:#eef2f7; padding:4px 10px; border-radius:12px;">Borrower Portal</span>
→
<span style="background:#f5f7fa; padding:4px 10px; border-radius:12px;">Audit Logs</span>

</div>
""")
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Development Server")
                ))

                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token. Format: Bearer <token>")
                        )
                );
    }
}
