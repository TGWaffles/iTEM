package club.thom.tem.backend.requests;

public interface BackendRequest {

    BackendResponse makeRequest();

    int hashCode();

    boolean equals(Object o);

}
